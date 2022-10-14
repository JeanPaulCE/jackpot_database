package com.pi_libraries.jackpot_database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/** Registro
// Administa el flujo de datos entre la app y la DB
// pasa la informacion de la tabla espesificada al crear el objeto a un array list el cual
// facilita operar sobre los datos sin nesesidad de sql, de forma volatil y util para usos tipios de android
//
// @Prop  ArrayList<Entidad> listaEntidades: alamacena los datos de la tabla en un Array lis generico pero conteniendo los objetos espesificos de la tabla
//
// @Prop SQLiteDatabase DB: es una instancia de la base de datos utilizabnle la clase DataBase solo funciona para crear este objeto
//
// @Prop String tabla: es la tabla que se asigna al crear la instancia que usara para las consultas a la base de datos
//
// @Prop ArrayList<String> columnas: lista de columnas de la tabla espesifiacada en la tabla asignada
//
// @Prop Class hijo: Es la calse de un objeto hijo a entidad que referencia una tabla en la base de datos
//
// */

public class Registro {


    protected ArrayList<Entidad> listaEntidades;
    protected SQLiteDatabase DB;
    protected String tabla;
    protected ArrayList<String> columnas;
    protected Class hijo;
    public static String DESC = "DESC";
    public static String ASC = "ASC";
    private String orderby = DESC;

    private void constructor(Class Hijo_) {
        try {//se intenta obtener la tabla que se queire manejar en cada isntancai
            this.hijo = Hijo_;
            this.tabla = (String) Hijo_.getDeclaredField("Tabla").get(null);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        listaEntidades = new ArrayList<Entidad>();
        this.DB = (new DataBase( null, 1)).getWritableDatabase();//crea Db
        loadDb();//carga los datos a listaEntidades
        getColumnas();
    }

    public Registro(Class Hijo_,String orderby){
        this.orderby = orderby;
        constructor(Hijo_);
    }



    public Registro(Class Hijo_) {//objetos entidad en el arraylsit con casting a la calse espesificada como hijo
        constructor(Hijo_);
    }



    public Registro(String tabla) {//objetos genericos Entidad en el arraylsit sin posibilidad de casting
        this.tabla = tabla;
        listaEntidades = new ArrayList<Entidad>();
        this.DB = (new DataBase( null, 1)).getWritableDatabase();
        loadDb();
        getColumnas();
    }

    private void getColumnas(){
        Cursor cursor = DB.rawQuery ("SELECT * FROM "+tabla+";" ,null);//carga las columnas de la base de datos
        columnas = new ArrayList<String>( (List<String>)Arrays.asList(cursor.getColumnNames()) );
    }

    public boolean add( Entidad entidad){

        boolean res=false;
        if (!listaEntidades.contains(entidad)) {
            listaEntidades.add(entidad);
            res = this.DB.insert(tabla, null, entidad.getContent()) > 0;

        }
        loadDb();
        return res;

    }

    //busqueda personalizada por propiedad
    public Entidad search(String columna, Object value){
        if (columnas.contains(columna)){
            for (Entidad entidad:listaEntidades) {
                if (entidad.getContent().get(columna).equals(value)){
                    return entidad;
                }
            }
        }
        return null;
    }

    public void delete(Entidad entidad){
        listaEntidades.remove(entidad);
        this.DB.delete(tabla,"id="+entidad.getId(),null);
    }

    public void update(Entidad entidad){

        listaEntidades.set(getIndex(entidad.getId()),entidad);
        this.DB.update(tabla,entidad.getContent(),"id="+entidad.getId(),null);
    }

    private int getIndex(int id) {
        return  listaEntidades.indexOf(search(id));
    }



    //busca por id
    public Entidad search(int id) {
        for (Entidad entidad: listaEntidades) {
            if (entidad.getId()==id)return entidad;
        }
        return null;
    }

    //obtiene la posicion de un elemetno en el Arraylist local
    public Entidad search_posicion(int i) {
        return listaEntidades.get(i);
    }


    //obtiene todos los datos de la base de datos y lo pasa al arraylist
    public void loadDb (){
        Cursor cursor = DB.rawQuery ("SELECT * FROM "+tabla+" ORDER BY id "+orderby+" ;" ,null);
        cursor.moveToFirst();
        listaEntidades = new ArrayList<Entidad>();

        while(!cursor.isAfterLast()){
            ContentValues data = new ContentValues();
            Object entidad = new Entidad();
            try {
                if (hijo!=null){
                    entidad = hijo.newInstance();
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }

            for (String columna:cursor.getColumnNames()) {
                int index = cursor.getColumnIndex(columna);
                int type = cursor.getType(index);
                if (!columna.equalsIgnoreCase("id")){
                    switch (type){
                        case Cursor.FIELD_TYPE_INTEGER:
                            int INTEGER  = cursor.getInt(index);
                            data.put(columna,INTEGER);
                            break;
                        case  Cursor.FIELD_TYPE_BLOB:
                            byte[] BLOB  = cursor.getBlob(index);
                            data.put(columna,BLOB);
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            float FLOAT  = cursor.getFloat(index);
                            data.put(columna,FLOAT);
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            String string  = cursor.getString(index);
                            data.put(columna,string);
                            break;
                        default:
                            break;
                    }
                }else {
                    ((Entidad)entidad).setId(cursor.getInt(index));
                }
            }

            ((Entidad)entidad).setContenido(data);
            listaEntidades.add((Entidad) entidad);
            cursor.moveToNext();
        }
        cursor.close();
    }

    public ArrayList<Entidad> getListaEntidades() {
        return listaEntidades;
    }
}
