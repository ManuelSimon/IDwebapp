<%@ page import="com.msnovoa.jaas.autenticacion.MsnovoaUserPrincipal" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Arrays" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
    MsnovoaUserPrincipal msnovoaUserPrincipal = (MsnovoaUserPrincipal) request.getUserPrincipal();

    String[] allRoles = {"R1","R2","R3", "R4", "R5"};
    ArrayList userRoles = new ArrayList(allRoles.length);
    for(String role : allRoles) {
        if(request.isUserInRole(role)) {
            userRoles.add(role);
        }
    }
    String userRolesString = String.join(", ", userRoles);

    String rGestionNominas = (userRolesString.contains("R3")) ? "✔️" : "❌";
    String rGestionTrabajadores = (userRolesString.contains("R3") || userRolesString.contains("R5")) ? "✔️" : "❌";
    String rAutorizarCompras = (userRolesString.contains("R1")) ? "✔️" : "❌";
    String rGestionCompras = (userRolesString.contains("R4")) ? "✔️" : "❌";
    String rGestionProveedores = (userRolesString.contains("R4") || userRolesString.contains("R5")) ? "✔️" : "❌";
    String rGestionClientes = (userRolesString.contains("R2")) ? "✔️" : "❌";
    String rGestionFacturas = (userRolesString.contains("R2")) ? "✔️" : "❌";
    String rGestionPresupuestos = (userRolesString.contains("R2") || userRolesString.contains("R5")) ? "✔️" : "❌";
%>

<html>
<head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title>Home Page</title>
</head>
<body>
<div id="content">
    <h1>Sistema de Gestión</h1>

    <h2>¡Hola, <%= msnovoaUserPrincipal.getName() %>!</h2>


    <hr>
    <h2>Información del usuario:</h2>
    <p><b>Nombre de usuario:</b> <%= msnovoaUserPrincipal.getName() %></p>
    <p><b>Nombre completo:</b> <%= msnovoaUserPrincipal.getRealName() %></p>
    <p><b>Posición en la empresa:</b> <%= msnovoaUserPrincipal.getPosition() %></p>
    <p><b>Roles:</b> <%= userRolesString %></p>

    <hr>
    <h2>Menú:</h2>

    <h3>Módulo Ventas</h3></td>
    <p><%= rGestionClientes %> <a href="ventas/gestion_clientes.jsp">Operación: gestionar clientes</a></p>
    <p><%= rGestionPresupuestos %> <a href="ventas/gestion_presupuestos.jsp">Operación: gestionar presupuestos</a></p>
    <p><%= rGestionFacturas %> <a href="ventas/gestion_facturas.jsp">Operación: gestionar facturas</a></p>

    <h3>Módulo Compras</h3></td>
    <p><%= rGestionProveedores %> <a href="compras/gestion_proveedores.jsp">Operación: gestionar proveedores</a></p>
    <p><%= rGestionCompras %> <a href="compras/gestion_compras.jsp">Operación: gestionar compras</a></p>
    <p><%= rAutorizarCompras %> <a href="compras/autorizar_compras.jsp">Operación: autorizar compras</a></p>


    <h3>Módulo Nóminas</h3></td>
    <p><%= rGestionTrabajadores %> <a href="nominas/gestion_trabajadores.jsp">Operación: gestionar trabajadores</a></p>
    <p><%= rGestionNominas %> <a href="nominas/gestion_nominas.jsp">Operación: gestionar nóminas</a></p>

    <hr>
    <h2>¿Deseas irte ya?</h2>
    <p><a href="${pageContext.request.contextPath}/logout">Cerrar sesión</a></p>
</div>
</body>
</html>
