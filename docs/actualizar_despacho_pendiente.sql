-- Actualización no destructiva del esquema existente. Conserva despachos y ventas.
USE SistemaVentas;
SET XACT_ABORT ON;
BEGIN TRY
    BEGIN TRANSACTION;
    IF OBJECT_ID('dbo.CK_despacho_completo','C') IS NOT NULL
        ALTER TABLE dbo.DetalleDespacho DROP CONSTRAINT CK_despacho_completo;
    ALTER TABLE dbo.DetalleDespacho WITH CHECK ADD CONSTRAINT CK_despacho_completo
        CHECK (cantidad_despachada = 0 OR cantidad_despachada = cantidad_solicitada);
    COMMIT;
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    THROW;
END CATCH;
