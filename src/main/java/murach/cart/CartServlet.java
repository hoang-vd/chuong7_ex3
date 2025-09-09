package murach.cart;

import murach.data.ProductIO;
import murach.business.LineItem;
import murach.business.Cart;
import murach.business.Product;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;


public class CartServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        
        String url = "/index.jsp";
        ServletContext sc = getServletContext();
        
        // get current action
        String action = request.getParameter("action");
        if (action == null) {
            action = "cart";  // default action
        }

        // perform action and set URL to appropriate page
        if (action.equals("shop")) {
            url = "/index.jsp";    // the "index" page
        }
        else if (action.equals("cart")) {
            String productCode = request.getParameter("productCode");
            String quantityString = request.getParameter("quantity");

            HttpSession session = request.getSession();
            Cart cart = (Cart) session.getAttribute("cart");
            if (cart == null) {
                cart = new Cart();
            }

            // Xử lý số lượng
            int quantity;
            try {
                quantity = Integer.parseInt(quantityString);
                if (quantity < 0) {
                    quantity = 1;
                }
            } catch (NumberFormatException nfe) {
                quantity = 1;
            }

            String path = sc.getRealPath("/WEB-INF/products.txt");
            Product product = ProductIO.getProduct(productCode, path);

            // Lấy quantity cũ
            int oldQuantity = 0;
            for (LineItem it : cart.getItems()) {
                if (it.getProduct().getCode().equals(productCode)) {
                    oldQuantity = it.getQuantity();
                    break;
                }
            }

            boolean isUpdate = request.getParameter("update") != null;

            // ✅ Kiểm tra Remove trước khi cộng dồn
            if (quantity == 0) {
                cart.removeItemByCode(productCode);
            } else {
                // Nếu không phải update (nghĩa là Add To Cart) → cộng dồn
                if (!isUpdate) {
                    quantity = oldQuantity + quantity;
                }

                LineItem lineItem = new LineItem();
                lineItem.setProduct(product);
                lineItem.setQuantity(quantity);

                cart.addItem(lineItem);
            }

            session.setAttribute("cart", cart);
            url = "/cart.jsp";
        }
        else if (action.equals("checkout")) {
            url = "/checkout.jsp";
        }

        sc.getRequestDispatcher(url)
                .forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
}