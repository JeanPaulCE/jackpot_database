package com.pi_libraries.jackpot_database;


import android.content.Context;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**Creacion de la base de datos*/
public class DataBase  extends SQLiteOpenHelper{

    protected static  String BaseDatos = "Default";
    protected static Context context;
    protected static ArrayList<String> Tablas = new ArrayList<String>();
    protected static ArrayList<String> Sql = new ArrayList<String>();
    private final static  String FIELD_TYPE_INTEGER = Integer.class.getName();
    private final static  String FIELD_TYPE_INT = "int";
    private final static  String FIELD_TYPE_BLOB = byte[].class.getName();
    private final static  String FIELD_TYPE_FLOAT = float.class.getName();
    private final static  String FIELD_TYPE_STRING = String.class.getName();


    public DataBase( @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, BaseDatos, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase baseDeDatos) {
        //Verificar que se permitan primari keys
        for (String sql:Sql) {
            baseDeDatos.execSQL(sql);
        }
        for (String tabla:Tablas) {
            baseDeDatos.execSQL(tabla);
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    //Ajustes basico para el funcionamiento de la Base de datos
    public static void setContext(Context context) {
        DataBase.context = context;
    }

    //Ajustes basico para el funcionamiento de la Base de datos
    public static void setName(String baseDatos) {
        BaseDatos = baseDatos;
    }

    public static void addSql(String sql)  {
        Sql.add(sql);
    }


    public static void addTabla(@NonNull Class entidad)  {

        try {
            String parametros = "";

            String tabla = (String) entidad.getDeclaredField("Tabla").get(null);
            Method[] methods = entidad.getMethods();
            Metodo[] metodos = new Metodo[methods.length];
            ArrayList<String> columnas  = new ArrayList<String>();
            for (int contador = 0; contador < methods.length; contador++) {
                metodos[contador] = new Metodo();
                metodos[contador].nombre = methods[contador].getName();

                    metodos[contador].parametros = methods[contador].getParameterTypes();

            }

            for (int index = 0; index < metodos.length; index++) {
                String posibleColumna = metodos[index].nombre;
                Pattern pattern = Pattern.compile("(^set)_([Ññ_A-Za-z-]+)");
                Matcher matcher = pattern.matcher(posibleColumna);
                if (matcher.matches()){
                    String columna = matcher.group(2);
                    String type = "";
                    if (metodos[index].parametros!=null){
                        String type__ = metodos[index].parametros[0].getName();


                        if (type__.equalsIgnoreCase(FIELD_TYPE_INTEGER)) {
                            type = "INTEGER";
                        }else if(type__.equalsIgnoreCase(FIELD_TYPE_INT)){
                            type = "INTEGER";
                        }else if (type__.equalsIgnoreCase(FIELD_TYPE_STRING)){
                            type = "TEXT";
                        }else if (type__.equalsIgnoreCase(FIELD_TYPE_FLOAT)){
                            type = "REAL";
                        }else if (type__.equalsIgnoreCase(FIELD_TYPE_BLOB)){
                            type = "BLOB";
                        }
                    }


                    if (columna.length()>0&&type.length()>0){
                        String sql = columna.toLowerCase()+" "+type;
                        columnas.add(sql);
                    }
                }

            }

            for (String columna:columnas) {
                parametros = parametros+" , "+columna;
            }
            String sqlLine = "Create Table IF NOT EXISTS  "+ tabla +" (id INTEGER primary key AUTOINCREMENT UNIQUE  "+parametros+");";
            Tablas.add(sqlLine);
            Log.d("Resultado",sqlLine);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }
    public static void addTablas(@NonNull Class[] entidades)  {
        for (Class entidad:entidades) {
            addTabla(entidad);
        }
    }

    public static void config(Context context,String baseDatos,Class[] entidades)  {
        addTablas(entidades);
        setName(baseDatos);
        setContext(context);
    }


}


