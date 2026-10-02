-- DESTRUCTIVO: borra únicamente SistemaVentas y todos sus datos de prueba.
-- Haga un respaldo si necesita conservar información. Ejecución explícita en SSMS/sqlcmd.
USE master;
GO
IF DB_ID(N'SistemaVentas') IS NOT NULL
BEGIN
    ALTER DATABASE SistemaVentas SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE SistemaVentas;
END;
GO
-- A continuación ejecute script_base_datos_ventas.sql.
