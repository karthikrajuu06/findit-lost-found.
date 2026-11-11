   package com.findit;

   import com.findit.util.DatabaseConnection;

   public class DbTest {
       public static void main(String[] args) throws Exception {
           try (var con = DatabaseConnection.getConnection()) {
               System.out.println("Connected to: " + con.getCatalog());
           }
       }
   }