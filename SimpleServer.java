import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class SimpleServer {
    private static final int PORT = 8080;
    private static final String WEB_ROOT = ".";
    
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("🚀 University Cafeteria Server started on http://localhost:" + PORT);
            System.out.println("📁 Serving files from: " + new File(WEB_ROOT).getAbsolutePath());
            System.out.println("🌐 Open your browser and go to: http://localhost:" + PORT + "/main.html");
            System.out.println("⏹️  Press Ctrl+C to stop the server");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleRequest(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }
    
    private static void handleRequest(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            String requestLine = in.readLine();
            if (requestLine == null) return;
            
            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 2) return;
            
            String method = requestParts[0];
            String path = requestParts[1];
            
            // Handle API endpoints
            if (path.startsWith("/api/")) {
                handleApiRequest(out, method, path);
                return;
            }
            
            // Serve static files
            serveStaticFile(out, path);
            
        } catch (IOException e) {
            System.err.println("Error handling request: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }
    
    private static void handleApiRequest(PrintWriter out, String method, String path) {
        // Set CORS headers
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: application/json");
        out.println("Access-Control-Allow-Origin: *");
        out.println("Access-Control-Allow-Methods: GET, POST, OPTIONS");
        out.println("Access-Control-Allow-Headers: Content-Type");
        out.println();
        
        // Mock API responses
        String response = "{\"success\": true, \"message\": \"API endpoint working\"}";
        
        switch (path) {
            case "/api/login":
                response = "{\"success\": true, \"message\": \"Login successful\", \"userType\": \"student\", \"email\": \"test@example.com\", \"name\": \"Test User\"}";
                break;
            case "/api/register":
                response = "{\"success\": true, \"message\": \"Registration successful\"}";
                break;
            case "/api/menu":
                response = "{\"success\": true, \"items\": [" +
                    "{\"item_id\": 1, \"name\": \"Classic Burger\", \"price\": 8.99, \"description\": \"Juicy beef patty with lettuce, tomato, and our special sauce\"}," +
                    "{\"item_id\": 2, \"name\": \"Margherita Pizza\", \"price\": 12.99, \"description\": \"Fresh mozzarella, tomato sauce, and basil on thin crust\"}," +
                    "{\"item_id\": 3, \"name\": \"Caesar Salad\", \"price\": 7.99, \"description\": \"Crisp romaine lettuce with parmesan and croutons\"}," +
                    "{\"item_id\": 4, \"name\": \"Chicken Wrap\", \"price\": 6.99, \"description\": \"Grilled chicken with vegetables in a soft tortilla\"}," +
                    "{\"item_id\": 5, \"name\": \"French Fries\", \"price\": 3.99, \"description\": \"Golden crispy fries with sea salt\"}," +
                    "{\"item_id\": 6, \"name\": \"Coca Cola\", \"price\": 2.49, \"description\": \"Refreshing cola drink\"}," +
                    "{\"item_id\": 7, \"name\": \"Coffee\", \"price\": 2.99, \"description\": \"Freshly brewed coffee\"}," +
                    "{\"item_id\": 8, \"name\": \"Chocolate Cake\", \"price\": 4.99, \"description\": \"Rich chocolate cake with frosting\"}" +
                "]}";
                break;
            case "/api/loyaltyPoints":
                response = "{\"success\": true, \"points\": 50}";
                break;
            case "/api/pendingOrders":
                response = "{\"success\": true, \"orders\": []}";
                break;
            default:
                response = "{\"success\": false, \"message\": \"Endpoint not implemented\"}";
        }
        
        out.println(response);
    }
    
    private static void serveStaticFile(PrintWriter out, String path) {
        try {
            if (path.equals("/")) {
                path = "/main.html";
            }
            
            File file = new File(WEB_ROOT + path);
            
            if (!file.exists() || !file.isFile()) {
                send404(out);
                return;
            }
            
            String contentType = getContentType(path);
            byte[] content = Files.readAllBytes(file.toPath());
            
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: " + contentType);
            out.println("Content-Length: " + content.length);
            out.println();
            out.flush();
            
            // Send file content
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    out.write(new String(buffer, 0, bytesRead));
                }
            }
            
        } catch (IOException e) {
            send500(out);
        }
    }
    
    private static String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".json")) return "application/json";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        return "text/plain";
    }
    
    private static void send404(PrintWriter out) {
        out.println("HTTP/1.1 404 Not Found");
        out.println("Content-Type: text/html");
        out.println();
        out.println("<html><body><h1>404 - File Not Found</h1></body></html>");
    }
    
    private static void send500(PrintWriter out) {
        out.println("HTTP/1.1 500 Internal Server Error");
        out.println("Content-Type: text/html");
        out.println();
        out.println("<html><body><h1>500 - Internal Server Error</h1></body></html>");
    }
}
