import java.sql.*;
public class TestJdbc {
  public static void main(String[] args) throws Exception {
    String url = "jdbc:oracle:thin:@//192.168.1.100:1521/m20";
    String user = "DEV_DEPL";
    String pass = "DEV04";
    Connection c = DriverManager.getConnection(url, user, pass);
    String sql = "SELECT 1 FROM DUAL";
    try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
      while (rs.next()) System.out.println(rs.getInt(1));
    }
    c.close();
  }
}
