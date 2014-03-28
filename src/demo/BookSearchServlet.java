package demo;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class BookSearchServlet
 */
@WebServlet("/BookSearch")
public class BookSearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public BookSearchServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		String bookName = request.getParameter("searchbook");  
	    if(bookName==null) {  
	        response.getWriter().write("[['Illegal Param']]");  
	        return;  
	    }  
	    String jav = "['java �s�{��Q'], ['Java �J��'], ['JavaScript �{�ǳ]�p']";  
	    String cpp = "['C++�s�{��Q'],['C++�J��'],['C++�{�ǳ]�p']";  
	    String php = "['php �{�ǳ]�p'],['php �J��'],['php �q�J�����q']";  
	    String books = "";  
	    if(bookName.equals("allbook"))  
	    {  
	        books = "["+jav+","+cpp+","+php+"]";  
	        response.getWriter().write(books);  
	    }  
	    else  
	    {  
	        bookName = bookName.substring(0,3);  
	        if(bookName.equals("jav"))  
	        {  
	            books = "["+jav+"]";  
	        } else if(bookName.equals("c++")) {  
	            books = "["+cpp+"]";  
	        } else if(bookName.equals("php")) {  
	            books = "["+php+"]";  
	        } else {  
	            books = "[['No data']]";  
	        }  
	        response.getWriter().write(books);  
	    }  
	}
}
