/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ConnectDB;
import java.sql.*;

/**
 *
 * @author 84941
 */
public class ConnectionUtils {
    public static Connection getMyConnection() throws ClassNotFoundException, SQLException {
        return ConnectionOracle.getOracleConnection();
    }
}
