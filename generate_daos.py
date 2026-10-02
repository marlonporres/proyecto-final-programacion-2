import os

base_dir = r'C:\Users\marlo\UNIVERSIDAD\2026\SEGUNDO SEMESTRE 2026\Programacion_2\sistema-ventas-proyecto\proyecto-final-programacion-2\src\main\java\gt\edu\umg\ventas\dao'
entities = ['OrdenVenta', 'Bodega', 'Despacho', 'ExistenciaInventario', 'MovimientoInventario']

for entity in entities:
    # Interface
    interface_content = f'''package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.{entity};
import java.util.List;

public interface {entity}DAO {{
    void crear({entity} {entity.lower()});
    {entity} obtener(int id);
    List<{entity}> obtenerTodos();
    void actualizar({entity} {entity.lower()});
    void eliminar(int id);
}}
'''
    with open(os.path.join(base_dir, f'{entity}DAO.java'), 'w') as f:
        f.write(interface_content)

    # Impl
    impl_content = f'''package gt.edu.umg.ventas.dao;

import gt.edu.umg.ventas.modelo.{entity};
import java.util.List;
import java.util.ArrayList;
import java.sql.*;

public class {entity}DAOImpl implements {entity}DAO {{
    @Override
    public void crear({entity} {entity.lower()}) {{
        // TODO: Implement
    }}

    @Override
    public {entity} obtener(int id) {{
        // TODO: Implement
        return null;
    }}

    @Override
    public List<{entity}> obtenerTodos() {{
        // TODO: Implement
        return new ArrayList<>();
    }}

    @Override
    public void actualizar({entity} {entity.lower()}) {{
        // TODO: Implement
    }}

    @Override
    public void eliminar(int id) {{
        // TODO: Implement
    }}
}}
'''
    with open(os.path.join(base_dir, f'{entity}DAOImpl.java'), 'w') as f:
        f.write(impl_content)

print('DAOs generated.')
