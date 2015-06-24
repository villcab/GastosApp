package com.villcab.gastos.utils.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.googlecode.openbeans.PropertyDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class Wrapper extends SQLiteOpenHelper {

    private SQLiteDatabase connection = null;

    protected Context context;

    public Wrapper(Context context) {
        super(context, Config.databaseName, null, 1);

        this.context = context;
    }

    public Wrapper(Context context, SQLiteDatabase connection) {
        super(context, Config.databaseName, null, 1);
        this.connection = connection;
    }

    public int count(String strQuery) {
        SQLiteDatabase objDB = this.getReadableDatabase();
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = objDB.rawQuery(strQuery, new String[]{});
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    count = cursor.getInt(0);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            objDB.close();
        }
        return count;
    }

    public void clean(Entity entity) {
        SQLiteDatabase objDb = this.getWritableDatabase();
        String table = getTableName(entity);
        try {
            objDb.beginTransaction();
            objDb.delete(table, "", new String[]{});
            objDb.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("debug", e.getMessage());
        } finally {
            objDb.endTransaction();
            objDb.close();
        }
    }

    private static boolean Ignore(Field field) {
        Annotation[] annotations = field.getAnnotations();
        if (annotations.length > 0) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof Ignore) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean save(Entity entity) {
        boolean result = false;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values;
        String table = getTableName(entity);
        try {
            db.beginTransaction();
            values = new ContentValues();
            Field[] entityFields = entity.getClass().getDeclaredFields();
            Field[] superFields = entity.getClass().getSuperclass().getDeclaredFields();
            List<Field> listFields = new ArrayList<Field>();
            listFields.addAll(Arrays.asList(entityFields));
            listFields.addAll(Arrays.asList(superFields));

            for (Field field : listFields) {
                if (!Ignore(field)) {
                    PropertyDescriptor prop;
                    try {
                        prop = new PropertyDescriptor(field.getName(), entity.getClass());
                    } catch (NullPointerException e) {
                        prop = new PropertyDescriptor(field.getName(), entity.getClass().getSuperclass());
                    }
                    Method method = prop.getReadMethod();
                    Object value = method.invoke(entity);
                    if (value != null) {
                        values.put(field.getName(), value.toString());
                    }
                    // REVISAR ESTA SENTENCIA ELSE AGREGADA PARA EVITAR QUE ALGUN CAMPO CON VALOR NULO NO SE INSERTE
                    //else {
                    //values.put(field.getName(), "null");
                    //}
                }
            }
            if (entity.getAction() == Action.INSERT) {
                entity.setId(db.insert(table, null, values));
            } else if (entity.getAction() == Action.UPDATE) {
                db.update(table, values, "id=?", new String[]{Long.toString(entity.getId())});
            } else {
                db.delete(table, "id=?", new String[]{Long.toString(entity.getId())});
            }
            db.setTransactionSuccessful();
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }

        return result;
    }

    public static String getCreate(Entity entity) {
        Field[] fields = entity.getClass().getDeclaredFields();
        Field[] superFields = entity.getClass().getSuperclass().getDeclaredFields();
        List<Field> listFields = new ArrayList<Field>();
        listFields.addAll(Arrays.asList(fields));
        listFields.addAll(Arrays.asList(superFields));
        fields = new Field[listFields.size()];
        fields = listFields.toArray(fields);
        List<Object> columns = new ArrayList<Object>();
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE ");
        builder.append(getTableName(entity));
        try {
            for (Field field : fields) {
                if (!(field.getName().equals("action"))) {
                    if (!Ignore(field)) {
                        Annotation[] annotations = field.getAnnotations();
                        if (annotations.length > 0) {
                            for (Annotation annotation : annotations) {
                                if (annotation instanceof Key) {
                                    columns.add(field.getName() + " integer NOT NULL PRIMARY KEY AUTOINCREMENT");
                                } else if (annotation instanceof Nullable) {
                                    columns.add(field.getName() + " text");
                                } else {
                                    columns.add(field.getName() + " text NOT NULL");
                                }
                            }
                        } else {
                            columns.add(field.getName() + " text");
                        }
                    }
                }

            }
            builder.append(concatKeys(columns));
            return builder.toString();
        } catch (Exception e) {
            Log.e("hola", e.getMessage());
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    protected <T extends Entity> List<T> list(String query, Entity entity) {
        List<T> lstResult = new ArrayList<T>();
        SQLiteDatabase objDb = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = objDb.rawQuery(query, new String[]{});
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    lstResult.add((T) this.load(cursor, entity).getClone());
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("hola", e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        objDb.close();
        this.close();
        return lstResult;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected List<Map> genericList(String strQuery) {
        List<Map> lstResult = new ArrayList<Map>();
        Map obj;
        SQLiteDatabase objDb = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = objDb.rawQuery(strQuery, new String[]{});
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    obj = new HashMap();
                    for (int index = 0; index < cursor.getColumnCount(); index++) {
                        obj.put(cursor.getColumnName(index), cursor.getString(index));
                    }
                    lstResult.add(obj);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("hola", e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        objDb.close();
        this.close();
        return lstResult;
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> T load(Cursor cursor, Entity entity) {
        T obj = (T) entity;
        Field[] fields = obj.getClass().getDeclaredFields();
        Field[] superFields = obj.getClass().getSuperclass().getDeclaredFields();
        List<Field> listFields = new ArrayList<Field>();
        listFields.addAll(Arrays.asList(fields));
        listFields.addAll(Arrays.asList(superFields));
        try {
            for (Field field : listFields) {
                if (!Ignore(field)) {
                    PropertyDescriptor prop;
                    try {
                        prop = new PropertyDescriptor(field.getName(), obj.getClass());
                    } catch (NullPointerException e) {
                        prop = new PropertyDescriptor(field.getName(), obj.getClass().getSuperclass());
                    }
                    Method method = prop.getWriteMethod();

                    if (method != null) {
                        Type type = field.getGenericType();
                        if (type.toString().equals(String.class.toString())) {
                            String value = cursor.getString(cursor.getColumnIndex(field.getName()));
                            method.invoke(obj, new Object[]{value});
                        } else if (type.toString().equals(Long.class.toString())) {
                            Long value = cursor.getLong(cursor.getColumnIndex(field.getName()));
                            method.invoke(obj, new Object[]{value});
                        } else if (type.toString().equals(Integer.class.toString())) {
                            Integer value = cursor.getInt(cursor.getColumnIndex(field.getName()));
                            method.invoke(obj, new Object[]{value});
                        } else if (type.toString().equals(Double.class.toString())) {
                            Double value = cursor.getDouble(cursor.getColumnIndex(field.getName()));
                            method.invoke(obj, new Object[]{value});
                        } else if (type.toString().equals(Date.class.toString())) {
                            String value = cursor.getString(cursor.getColumnIndex(field.getName()));
                            Date dateValue = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy", Locale.US).parse(value);
                            method.invoke(obj, new Object[]{dateValue});
                        }

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (T) obj;
    }

    protected static String concatKeys(List<Object> Llaves) {
        return ((Llaves.size() > 0) ? "(" + TextUtils.join(", ", Llaves) + ")" : "(o)");
    }

    protected static String concatKeysLong(List<Long> Llaves) {
        return ((Llaves.size() > 0) ? "(" + TextUtils.join(", ", Llaves) + ")" : "(0)");
    }

    public static String getTableName(Entity entity) {
        String[] buf = entity.getClass().getName().split("\\.");
        return (buf[buf.length - 2] + "_" + buf[buf.length - 1]).toLowerCase();
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> T get(String strQuery, Entity entity) {
        SQLiteDatabase objDb = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = objDb.rawQuery(strQuery, new String[]{});
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    entity = (T) this.load(cursor, entity);
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("hola", e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        objDb.close();
        this.close();
        return (T) entity;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(getCreate(new Venta()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public SQLiteDatabase getConnection() {
        return connection;
    }

    public void setConnection(SQLiteDatabase connection) {
        this.connection = connection;
    }

    public void openTransaction() {
        connection = this.getWritableDatabase();
        connection.beginTransaction();
    }

    public void transactionSuccesfully() {
        connection.setTransactionSuccessful();
    }

    public void closeTransaction() {
        connection.endTransaction();
        connection.close();
    }
}