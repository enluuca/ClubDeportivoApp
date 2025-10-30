package com.example.clubdeportivoapp.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.Locale;

import androidx.annotation.Nullable;

/**
 * Clase Java para la gestión de SQLite.
 * Define constantes públicas para nombres de tablas y columnas (necesarias para Kotlin)
 * e inserta datos de prueba para Socios Activos y Morosos.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ClubDB.db";
    // ¡CAMBIO CRÍTICO! Incrementamos la versión a 2 para forzar onUpgrade y recargar onCreate
    private static final int DATABASE_VERSION = 2;

    // ====================================================================
    // 1. CONSTANTES DE TABLAS Y COLUMNAS (ACCESIBLES DESDE KOTLIN)
    // ====================================================================

    // --- Tabla Usuarios (para Login) ---
    public static final String TABLE_USERS = "Usuarios";
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_USUARIO = "usuario";
    public static final String COL_USER_CLAVE = "clave";
    public static final String COL_USER_ROL = "rol";

    // --- Tabla Cliente ---
    public static final String TABLE_CLIENTE = "Cliente";
    public static final String CLIENTE_COL_ID = "id";
    public static final String CLIENTE_COL_NOMBRE = "nombre";
    public static final String CLIENTE_COL_APELLIDO = "apellido";
    public static final String CLIENTE_COL_DNI = "dni";
    public static final String CLIENTE_COL_FECHA_NACIMIENTO = "fechaNacimiento";
    public static final String CLIENTE_COL_DIRECCION = "direccion";
    public static final String CLIENTE_COL_TELEFONO = "telefono";
    public static final String CLIENTE_COL_APTO_FISICO = "aptoFisico"; // INTEGER (0/1)
    public static final String CLIENTE_COL_ASOCIARSE = "asociarse"; // INTEGER (0/1)
    public static final String CLIENTE_COL_FECHA_ALTA = "fechaAlta";

    // --- Tabla Socio ---
    public static final String TABLE_SOCIO = "Socio";
    public static final String SOCIO_COL_CLIENTE_ID = "id"; // FK a Cliente.id
    public static final String SOCIO_COL_FECHA_INSCRIPCION = "fechaInscripcion";
    public static final String SOCIO_COL_FECHA_VENCIMIENTO_CUOTA = "fechaVencimientoCuota";
    public static final String SOCIO_COL_NUMERO_CARNET = "numeroCarnet";
    public static final String SOCIO_COL_CARNET_ENTREGADO = "carnetEntregado"; // INTEGER (0/1)
    public static final String SOCIO_COL_FECHA_BAJA = "fechaBaja";

    // --- Tabla NoSocio ---
    public static final String TABLE_NO_SOCIO = "NoSocio";
    public static final String NO_SOCIO_COL_CLIENTE_ID = "id"; // FK a Cliente.id
    public static final String NO_SOCIO_COL_FECHA_BAJA = "fechaBaja";

    // --- Tabla Actividad ---
    public static final String TABLE_ACTIVIDAD = "Actividad";
    public static final String ACTIVIDAD_COL_ID = "id";
    public static final String ACTIVIDAD_COL_NOMBRE = "nombre";
    public static final String ACTIVIDAD_COL_COSTO = "costo"; // REAL

    // --- Tabla Cuota ---
    public static final String TABLE_CUOTA = "Cuota";
    public static final String CUOTA_COL_ID = "id";
    public static final String CUOTA_COL_ID_SOCIO = "idSocio";
    public static final String CUOTA_COL_FECHA_PAGO = "fechaPago";
    public static final String CUOTA_COL_MONTO = "monto";
    public static final String CUOTA_COL_MEDIO_PAGO = "medioPago";
    public static final String CUOTA_COL_CANTIDAD_CUOTAS = "cantidadCuotas";
    public static final String CUOTA_COL_DESCUENTO = "descuento";
    public static final String CUOTA_COL_MONTO_TOTAL = "montoTotal";
    public static final String CUOTA_COL_FECHA_VENCIMIENTO = "fechaVencimiento";
    public static final String CUOTA_COL_COMPROBANTE = "comprobante";

    // --- Tabla RegistroActividad ---
    public static final String TABLE_REGISTRO_ACTIVIDAD = "RegistroActividad";
    public static final String REG_ACT_COL_ID = "id";
    public static final String REG_ACT_COL_ID_NO_SOCIO = "idNoSocio";
    public static final String REG_ACT_COL_ID_ACTIVIDAD = "idActividad";
    public static final String REG_ACT_COL_FECHA_PAGO = "fechaPago";
    public static final String REG_ACT_COL_MONTO = "monto";
    public static final String REG_ACT_COL_MEDIO_PAGO = "medioPago";
    public static final String REG_ACT_COL_CANTIDAD_CUOTAS = "cantidadCuotas";
    public static final String REG_ACT_COL_DESCUENTO = "descuento";
    public static final String REG_ACT_COL_MONTO_TOTAL = "montoTotal";
    public static final String REG_ACT_COL_COMPROBANTE = "comprobante";

    // ====================================================================
    // 2. SENTENCIAS DE CREACIÓN DE TABLAS
    // ====================================================================

    private static final String CREATE_TABLE_CLIENTE =
            "CREATE TABLE " + TABLE_CLIENTE + " (" +
                    CLIENTE_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    CLIENTE_COL_NOMBRE + " TEXT," +
                    CLIENTE_COL_APELLIDO + " TEXT," +
                    CLIENTE_COL_DNI + " INTEGER UNIQUE," +
                    CLIENTE_COL_FECHA_NACIMIENTO + " TEXT," +
                    CLIENTE_COL_DIRECCION + " TEXT," +
                    CLIENTE_COL_TELEFONO + " TEXT," +
                    CLIENTE_COL_APTO_FISICO + " INTEGER," +
                    CLIENTE_COL_ASOCIARSE + " INTEGER," +
                    CLIENTE_COL_FECHA_ALTA + " TEXT" +
                    ")";

    private static final String CREATE_TABLE_SOCIO =
            "CREATE TABLE " + TABLE_SOCIO + " (" +
                    SOCIO_COL_CLIENTE_ID + " INTEGER PRIMARY KEY," +
                    SOCIO_COL_FECHA_INSCRIPCION + " TEXT," +
                    SOCIO_COL_FECHA_VENCIMIENTO_CUOTA + " TEXT," +
                    SOCIO_COL_NUMERO_CARNET + " INTEGER," +
                    SOCIO_COL_CARNET_ENTREGADO + " INTEGER," +
                    SOCIO_COL_FECHA_BAJA + " TEXT," +
                    "FOREIGN KEY (" + SOCIO_COL_CLIENTE_ID + ") REFERENCES " + TABLE_CLIENTE + "(" + CLIENTE_COL_ID + ")" +
                    ")";

    private static final String CREATE_TABLE_NO_SOCIO =
            "CREATE TABLE " + TABLE_NO_SOCIO + " (" +
                    NO_SOCIO_COL_CLIENTE_ID + " INTEGER PRIMARY KEY," +
                    NO_SOCIO_COL_FECHA_BAJA + " TEXT," +
                    "FOREIGN KEY (" + NO_SOCIO_COL_CLIENTE_ID + ") REFERENCES " + TABLE_CLIENTE + "(" + CLIENTE_COL_ID + ")" +
                    ")";

    private static final String CREATE_TABLE_ACTIVIDAD =
            "CREATE TABLE " + TABLE_ACTIVIDAD + " (" +
                    ACTIVIDAD_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    ACTIVIDAD_COL_NOMBRE + " TEXT," +
                    ACTIVIDAD_COL_COSTO + " REAL" +
                    ")";

    private static final String CREATE_TABLE_REGISTRO_ACTIVIDAD =
            "CREATE TABLE " + TABLE_REGISTRO_ACTIVIDAD + " (" +
                    REG_ACT_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    REG_ACT_COL_ID_NO_SOCIO + " INTEGER," +
                    REG_ACT_COL_ID_ACTIVIDAD + " INTEGER," +
                    REG_ACT_COL_FECHA_PAGO + " TEXT," +
                    REG_ACT_COL_MONTO + " REAL," +
                    REG_ACT_COL_MEDIO_PAGO + " TEXT," +
                    REG_ACT_COL_CANTIDAD_CUOTAS + " INTEGER," +
                    REG_ACT_COL_DESCUENTO + " REAL DEFAULT 0," +
                    REG_ACT_COL_MONTO_TOTAL + " REAL," +
                    REG_ACT_COL_COMPROBANTE + " INTEGER," +
                    "FOREIGN KEY (" + REG_ACT_COL_ID_NO_SOCIO + ") REFERENCES " + TABLE_NO_SOCIO + "(" + NO_SOCIO_COL_CLIENTE_ID + ")," +
                    "FOREIGN KEY (" + REG_ACT_COL_ID_ACTIVIDAD + ") REFERENCES " + TABLE_ACTIVIDAD + "(" + ACTIVIDAD_COL_ID + ")" +
                    ")";

    private static final String CREATE_TABLE_CUOTA =
            "CREATE TABLE " + TABLE_CUOTA + " (" +
                    CUOTA_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    CUOTA_COL_ID_SOCIO + " INTEGER," +
                    CUOTA_COL_FECHA_PAGO + " TEXT," +
                    CUOTA_COL_MONTO + " REAL," +
                    CUOTA_COL_MEDIO_PAGO + " TEXT," +
                    CUOTA_COL_CANTIDAD_CUOTAS + " INTEGER," +
                    CUOTA_COL_DESCUENTO + " REAL DEFAULT 0," +
                    CUOTA_COL_MONTO_TOTAL + " REAL," +
                    CUOTA_COL_FECHA_VENCIMIENTO + " TEXT," +
                    CUOTA_COL_COMPROBANTE + " INTEGER," +
                    "FOREIGN KEY (" + CUOTA_COL_ID_SOCIO + ") REFERENCES " + TABLE_SOCIO + "(" + SOCIO_COL_CLIENTE_ID + ")" +
                    ")";

    private static final String CREATE_TABLE_USUARIOS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COL_USER_USUARIO + " TEXT UNIQUE," +
                    COL_USER_CLAVE + " TEXT," +
                    COL_USER_ROL + " TEXT" +
                    ")";

    // ====================================================================
    // 3. MÉTODOS DE LA CLASE
    // ====================================================================

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CLIENTE);
        db.execSQL(CREATE_TABLE_SOCIO);
        db.execSQL(CREATE_TABLE_NO_SOCIO);
        db.execSQL(CREATE_TABLE_ACTIVIDAD);
        db.execSQL(CREATE_TABLE_REGISTRO_ACTIVIDAD);
        db.execSQL(CREATE_TABLE_CUOTA);
        db.execSQL(CREATE_TABLE_USUARIOS);

        insertDefaultUser(db);
        insertDefaultClientes(db); // Llama a la inserción de datos de prueba
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Al aumentar la versión, este código se ejecuta:
        // 1. Elimina todas las tablas existentes.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLIENTE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SOCIO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NO_SOCIO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVIDAD);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REGISTRO_ACTIVIDAD);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUOTA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        // 2. Llama a onCreate, que recrea las tablas E INSERTA LOS DATOS DE PRUEBA.
        onCreate(db);
    }

    private void insertDefaultUser(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COL_USER_USUARIO, "admin");
        values.put(COL_USER_CLAVE, "12345");
        values.put(COL_USER_ROL, "Administrador");
        db.insert(TABLE_USERS, null, values);
        Log.d("DatabaseHelper", "Usuario de prueba insertado.");
    }

    private void insertDefaultClientes(SQLiteDatabase db) {
        // Fechas de prueba
        String fechaActiva = "2026-06-30"; // Cuota al día
        String fechaMorosa = "2025-06-30"; // Cuota vencida

        // --- 1. 10 Clientes BASE (Socios y NoSocios) ---
        String[][] datosClientes = {
                // Socios Activos (6)
                {"Ana", "Gomez", "11111111", fechaActiva, "Socio Activo"},
                {"Luis", "Perez", "22222222", fechaActiva, "Socio Activo"},
                {"Maria", "Lopez", "33333333", fechaActiva, "Socio Activo"},
                {"Carlos", "Diaz", "44444444", fechaActiva, "Socio Activo"},
                {"Elena", "Ruiz", "55555555", fechaActiva, "Socio Activo"},
                {"Juan", "Mendez", "66666666", fechaActiva, "Socio Activo"},
                // Socios Morosos (4)
                {"Sofia", "Castro", "77777777", fechaMorosa, "Socio Moroso"},
                {"Pedro", "Gil", "88888888", fechaMorosa, "Socio Moroso"},
                {"Laura", "Vidal", "99999999", fechaMorosa, "Socio Moroso"},
                {"Andres", "Rojas", "10101010", fechaMorosa, "Socio Moroso"},
                // No Socios (5)
                {"Marta", "Nuñez", "11122233", "N/A", "No Socio"},
                {"Diego", "Sosa", "22334455", "N/A", "No Socio"},
                {"Paula", "Vazquez", "33445566", "N/A", "No Socio"},
                {"Javier", "Morales", "44556677", "N/A", "No Socio"},
                {"Noelia", "Flores", "55667788", "N/A", "No Socio"}
        };

        for (int i = 0; i < datosClientes.length; i++) {
            String[] cliente = datosClientes[i];
            ContentValues cv = new ContentValues();
            cv.put(CLIENTE_COL_NOMBRE, cliente[0]);
            cv.put(CLIENTE_COL_APELLIDO, cliente[1]);
            cv.put(CLIENTE_COL_DNI, Integer.parseInt(cliente[2]));
            cv.put(CLIENTE_COL_FECHA_NACIMIENTO, "1990-01-01");
            cv.put(CLIENTE_COL_DIRECCION, "Calle Falsa 123");
            cv.put(CLIENTE_COL_TELEFONO, "1155551234");
            cv.put(CLIENTE_COL_APTO_FISICO, 1); // True

            boolean isSocio = cliente[4].startsWith("Socio");
            cv.put(CLIENTE_COL_ASOCIARSE, isSocio ? 1 : 0);
            cv.put(CLIENTE_COL_FECHA_ALTA, "2024-01-01");

            long clienteId = db.insert(TABLE_CLIENTE, null, cv);

            if (clienteId != -1L) {
                if (isSocio) {
                    insertTestSocio(db, clienteId, cliente[3]);
                } else {
                    insertTestNoSocio(db, clienteId);
                }
            }
        }
        Log.d("DatabaseHelper", "Clientes de prueba insertados: 15.");
        // CONSULTA DE DEBUGGING
        printClientData(db);
    }

    private void insertTestSocio(SQLiteDatabase db, long clienteId, String fechaVencimiento) {
        ContentValues cvSocio = new ContentValues();
        cvSocio.put(SOCIO_COL_CLIENTE_ID, clienteId);
        cvSocio.put(SOCIO_COL_FECHA_INSCRIPCION, "2024-01-01");
        cvSocio.put(SOCIO_COL_FECHA_VENCIMIENTO_CUOTA, fechaVencimiento);
        cvSocio.put(SOCIO_COL_NUMERO_CARNET, (int) clienteId * 100);
        cvSocio.put(SOCIO_COL_CARNET_ENTREGADO, 1);
        cvSocio.put(SOCIO_COL_FECHA_BAJA, (String) null);
        db.insert(TABLE_SOCIO, null, cvSocio);
    }

    private void insertTestNoSocio(SQLiteDatabase db, long clienteId) {
        ContentValues cvNoSocio = new ContentValues();
        cvNoSocio.put(NO_SOCIO_COL_CLIENTE_ID, clienteId);
        cvNoSocio.put(NO_SOCIO_COL_FECHA_BAJA, (String) null);
        db.insert(TABLE_NO_SOCIO, null, cvNoSocio);
    }

    /**
     * Muestra datos de clientes en Logcat para depuración.
     */
    private void printClientData(SQLiteDatabase db) {
        Cursor cursor = null;
        try {
            String query = String.format(
                    "SELECT C.%s, C.%s, C.%s, C.%s, S.%s " +
                            "FROM %s C LEFT JOIN %s S ON C.%s = S.%s " +
                            "ORDER BY C.%s",
                    CLIENTE_COL_ID, CLIENTE_COL_NOMBRE, CLIENTE_COL_APELLIDO, CLIENTE_COL_DNI, SOCIO_COL_FECHA_VENCIMIENTO_CUOTA,
                    TABLE_CLIENTE, TABLE_SOCIO, CLIENTE_COL_ID, SOCIO_COL_CLIENTE_ID, CLIENTE_COL_ID);

            cursor = db.rawQuery(query, null);

            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String nombre = cursor.getString(1);
                String apellido = cursor.getString(2);
                int dni = cursor.getInt(3);
                String fechaVencimiento = cursor.getString(4); // Null para No Socios

                Log.d("DatabaseHelper",
                        String.format(Locale.getDefault(),
                                "DATOS CARGADOS: ID=%d, Nombre=%s %s, DNI=%d, Vencimiento=%s",
                                id, nombre, apellido, dni, fechaVencimiento != null ? fechaVencimiento : "N/A"
                        ));
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error al consultar datos de prueba: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    // ... (checkUser method remains the same)
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = { COL_USER_ID };
        String selection = COL_USER_USUARIO + " = ?" + " AND " + COL_USER_CLAVE + " = ?";
        String[] selectionArgs = { username, password };

        Cursor cursor = null;
        boolean isValid = false;

        try {
            cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                isValid = true;
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error al verificar usuario: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return isValid;
    }
}
