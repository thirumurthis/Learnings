 - Without maven, we need two jars
```
tdgssconfig.jar
teradatagc.jar
```

Make sure to place the libraries under the classpath or lib folder

- java code

```java
package com.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class TestTeraDBconnection {

	public static void main(String[] args) {

		//connection server name of db
		//check the docs at http://terahelp.blogspot.com/2009/08/connecting-to-database-using-teradata.html
		String connurl="jdbc:teradata://<database.db.com>/database=BackendDatabase";
		String user = "EFBDSS";
		String passwd = "EfbiDss1";
		//

		try {
			Class.forName("com.teradata.jdbc.TeraDriver");
			//new user id is 
			Connection conn = DriverManager.getConnection(connurl, user, passwd);
	
			String query= "select top 10 * from TABLE_IN_TERRA_DATA"; 

			PreparedStatement stmt=conn.prepareStatement(query);
			ResultSet rs=stmt.executeQuery();
			while(rs.next()) {
				String col1=rs.getString(1);
				System.out.println("col1="+col1);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}

```
