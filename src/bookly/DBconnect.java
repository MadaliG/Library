/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bookly;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import bookly.*;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author Machi
 */
public class DBconnect {

    Connection conn;

    public DBconnect() {
        try {
            // db parameters
            String url = "jdbc:sqlite:D://SQLite/Books.db";
            // create a connection to the database
            conn = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite has been established.");
//            Statement stmt = conn.createStatement();
//            ResultSet rs = stmt.executeQuery("SELECT * FROM carte");
//            while (rs.next()) {
////                String book = rs.getString("Titlu");
//                System.out.println(rs.getString("Titlu") + "\n");
//            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
//        finally {
//            try {
//                if (conn != null) {
//                    conn.close();
//                }
//            } catch (SQLException ex) {
//                System.out.println(ex.getMessage());
//            }
//        }
    }

    public ArrayList<Book> getBooks() throws SQLException {
        ArrayList<Book> books = new ArrayList();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT A.Prenume, A.Nume, C.Titlu, C.Locatie, C.CarteId, D.Gen FROM carte C, autor A, scrie S, domeniu D WHERE A.AutorID=S.AutorID and C.CarteID=S.CarteID and D.CarteID=C.CarteID");
        System.out.println("entering getBooks");
        while (rs.next()) {
            System.out.println(rs.getString("CarteId"));
            books.add(new Book(rs.getString("Prenume"), rs.getString("Nume"), rs.getString("Titlu"), rs.getString("CarteId"), rs.getString("Gen"), rs.getString("Locatie")));
        }
        System.out.println("wanna see books?");
        System.out.println(books);
        return books;
    }

    public void addBook(Book booker) throws SQLException {

        PreparedStatement myStmt = null;
        PreparedStatement query = null;
        PreparedStatement find_autorID = null;
        PreparedStatement final_query = null;
        PreparedStatement get_carteId = null;
        PreparedStatement get_autorId = null;
        PreparedStatement scrie_query = null;
        PreparedStatement book_query = null;

        try {
            //prepararea interogarii
            if ((booker.getCategory().trim().length() != 0) && (booker.getFirstName().trim().length() != 0)
                    && (booker.getLocation().trim().length() != 0) && (booker.getLastName().trim().length() != 0) && (booker.getTitle().trim().length() != 0)) {
                book_query = conn.prepareStatement("SELECT CarteID FROM carte WHERE Titlu like '%" + booker.getTitle() + "'");
                ResultSet book_query_result = book_query.executeQuery();
                if (!book_query_result.isBeforeFirst()) {
                    System.out.println("Nu exista nicio carte in baza de date. Sa adaugam una!");
                    myStmt = conn.prepareStatement("INSERT INTO carte"
                            + " (Titlu, Locatie) "
                            + " VALUES (?, ?); ");

                    //setarea parametrilor
                    myStmt.setString(1, booker.getTitle());
                    myStmt.setString(2, booker.getLocation());
                    myStmt.executeUpdate();
                }

                query = conn.prepareStatement("SELECT AutorID FROM autor WHERE Nume like '%" + booker.getLastName() + "'" + " and Prenume like '%" + booker.getFirstName() + "'");
                ResultSet query_result = query.executeQuery();
                System.out.println(booker.getFirstName());
                if (!query_result.isBeforeFirst()) {
                    System.out.println("Nu exista niciun autor in baza de date. Sa adaugam unul!");
                    find_autorID = conn.prepareStatement(" INSERT INTO autor (Nume, Prenume) VALUES ('" + booker.getLastName() + "','" + booker.getFirstName() + "') "
                    );
                    find_autorID.executeUpdate();
                };

                String carteId = null;
                String autorId = null;
                get_carteId = conn.prepareStatement("SELECT CarteID FROM carte WHERE Titlu='" + booker.getTitle() + "'");
                get_autorId = conn.prepareStatement("SELECT AutorID FROM autor WHERE Nume='" + booker.getLastName() + "' and Prenume='" + booker.getFirstName() + "'");
                ResultSet query_get_carteId = get_carteId.executeQuery();
                ResultSet query_get_autorId = get_autorId.executeQuery();
                if (query_get_carteId.next()) {
                    carteId = query_get_carteId.getString(1);
                }
                if (query_get_autorId.next()) {
                    autorId = query_get_autorId.getString(1);
                }

                scrie_query = conn.prepareStatement(" INSERT INTO scrie (AutorID, CarteID) "
                        + "VALUES (" + autorId + ", " + carteId + ") ");
                scrie_query.executeUpdate();
                System.out.println(scrie_query);

                final_query = conn.prepareStatement(" INSERT INTO domeniu (Gen, CarteID)  "
                        + " VALUES ( ?, " + carteId + " ); "
                );

                System.out.println(final_query);
                final_query.setString(1, booker.getCategory());
                final_query.executeUpdate();

                //executare interogare
//				System.out.print(query_result);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(myStmt, null);
            close(find_autorID, null);
            close(final_query, null);
            close(get_carteId, null);
            close(get_autorId, null);
            close(scrie_query, null);
        }
    }

    //stergere carte
    public void deleteBook(String CarteID) throws Exception {
        System.out.print(CarteID);
        PreparedStatement myStmt = null;
        PreparedStatement scrie_query = null;

        try {
            //preparea interogarii
            myStmt = conn.prepareStatement("DELETE FROM carte WHERE CarteID=" + CarteID);
            scrie_query = conn.prepareStatement("DELETE FROM scrie WHERE CarteID=" + CarteID);

            //executarea SQL
            scrie_query.executeUpdate();
            myStmt.executeUpdate();

        } finally {
            close(myStmt, null);
            close(scrie_query, null);
        }
    }

    //actualizare carte
    public void editBook(Book theBook) throws SQLException {
        PreparedStatement myBook = null;
        PreparedStatement myDomain = null;
        //prepararea interogarii
        System.out.println(theBook.getTitle());
        System.out.println(theBook.getLocation());
        System.out.println(theBook.getCategory());
        System.out.println(theBook.getBookId());
        myBook = conn.prepareStatement(" UPDATE Carte "
                + " SET Titlu = '" + theBook.getTitle() + "' , "
                + " Locatie ='" + theBook.getLocation()
                + "' WHERE CarteID = '" + theBook.getBookId() 
		+ "' ;  ");

        myDomain = conn.prepareStatement(" UPDATE Domeniu "
                + " SET Gen = '" + theBook.getCategory()
                + "' WHERE CarteID = '" + theBook.getBookId() + "' ;");

        //executararea SQL
        myBook.executeUpdate();
        myDomain.executeUpdate();

    }

    public String getLastBookId() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT MAX(id) FROM carte");
        String bookId = "";
        if (rs.next()) {
            bookId = rs.getString(1);
        }
        return bookId;
    }

    public void exportAsPDF(String filePath) throws SQLException, DocumentException, IOException {
//        Font font = Font.createFont(Font.TRUETYPE_FONT, new File("Times-Roman.tff"));
//        GraphicsEnvironment ge = 
//            GraphicsEnvironment.getLocalGraphicsEnvironment();
//        ge.registerFont(font);
        Font bold_font = new Font(FontFamily.TIMES_ROMAN, 10, Font.BOLD, BaseColor.BLACK);
        Font normal_font = new Font(FontFamily.TIMES_ROMAN, 10, Font.NORMAL, BaseColor.BLACK);

        Statement stmt = conn.createStatement();
        /* Define the SQL query */
        ResultSet rs = stmt.executeQuery("SELECT A.Prenume, A.Nume, C.Titlu, C.Locatie, C.CarteId, D.Gen FROM carte C, autor A, scrie S, domeniu D WHERE A.AutorID=S.AutorID and C.CarteID=S.CarteID and D.CarteID=C.CarteID");
        /* Step-2: Initialize PDF documents - logical objects */
        Document my_pdf_report = new Document();
        PdfWriter.getInstance(my_pdf_report, new FileOutputStream(filePath));
        my_pdf_report.open();
        //we have four columns in our table
        PdfPTable my_report_table = new PdfPTable(5);
        //create a cell object
        PdfPCell table_cell;
        //create table header
        table_cell = new PdfPCell(new Phrase("Prenume autor", bold_font));
        my_report_table.addCell(table_cell);
        table_cell = new PdfPCell(new Phrase("Nume autor", bold_font));
        my_report_table.addCell(table_cell);
        table_cell = new PdfPCell(new Phrase("Titlu", bold_font));
        my_report_table.addCell(table_cell);
        table_cell = new PdfPCell(new Phrase("Categorie", bold_font));
        my_report_table.addCell(table_cell);
        table_cell = new PdfPCell(new Phrase("Locatie", bold_font));
        my_report_table.addCell(table_cell);

        while (rs.next()) {
            String firstName = rs.getString("Prenume");
            table_cell = new PdfPCell(new Phrase(firstName, normal_font));
            my_report_table.addCell(table_cell);
            String lastName = rs.getString("Nume");
            table_cell = new PdfPCell(new Phrase(lastName, normal_font));
            my_report_table.addCell(table_cell);
            String title = rs.getString("Titlu");
            table_cell = new PdfPCell(new Phrase(title, normal_font));
            my_report_table.addCell(table_cell);
            String category = rs.getString("Gen");
            table_cell = new PdfPCell(new Phrase(category, normal_font));
            my_report_table.addCell(table_cell);
            String location = rs.getString("Locatie");
            table_cell = new PdfPCell(new Phrase(location, normal_font));
            my_report_table.addCell(table_cell);
        }
        /* Attach report table to PDF */
        my_pdf_report.add(my_report_table);
        my_pdf_report.close();

        /* Close all DB related objects */
        rs.close();
        stmt.close();
    }

    private static void close(Connection myConn, Statement myStmt, ResultSet myRs) throws SQLException {
        if (myRs != null) {
            myRs.close();
        }
        if (myStmt != null) {
        }
        if (myConn != null) {
            myConn.close();
        }
    }

    private void close(Statement myStmt, ResultSet myRs) throws SQLException {
        close(null, myStmt, myRs);
    }
}
