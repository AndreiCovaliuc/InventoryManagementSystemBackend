package com.example.inventory_backend.service.impl;

import com.example.inventory_backend.model.*;
import com.example.inventory_backend.service.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AIAssistantServiceImpl implements AIAssistantService {

    private final ChatClient chatClient;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final SupplierService supplierService;
    private final CategoryService categoryService;
    private final TransactionService transactionService;

    public AIAssistantServiceImpl(
            ChatModel chatModel,
            ProductService productService,
            InventoryService inventoryService,
            SupplierService supplierService,
            CategoryService categoryService,
            TransactionService transactionService) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.supplierService = supplierService;
        this.categoryService = categoryService;
        this.transactionService = transactionService;
    }

    @Override
    public String askQuestion(String question, Company company) {
        String context = buildContext(question, company);
        String systemPrompt = buildSystemPrompt();

        String fullPrompt = systemPrompt + "\n\n" + context + "\n\nUser Question: " + question;

        return chatClient.prompt()
                .user(fullPrompt)
                .call()
                .content();
    }

    private String buildSystemPrompt() {
        return """
            You are an intelligent inventory management assistant. Your role is to help users understand their inventory,
            products, suppliers, and stock levels. You have access to real-time data from the inventory system.

            Guidelines:
            - Provide clear, concise, and helpful answers
            - Use the provided data context to answer questions accurately
            - If asked about specific products, suppliers, or inventory items, reference the actual data
            - For numerical questions (totals, counts, averages), calculate from the provided data
            - If you don't have enough information to answer, say so clearly
            - Format numbers nicely (currency with 2 decimal places, quantities as whole numbers)
            - Be professional but friendly
            """;
    }

    private String buildContext(String question, Company company) {
        StringBuilder context = new StringBuilder();
        context.append("=== CURRENT INVENTORY DATA ===\n\n");

        // Always include summary stats
        context.append(buildSummaryStats(company));

        // Determine what detailed data to include based on the question
        String lowerQuestion = question.toLowerCase();

        if (containsAny(lowerQuestion, "stock", "inventory", "quantity", "low", "reorder", "level")) {
            context.append(buildInventoryContext(company));
        }

        if (containsAny(lowerQuestion, "product", "item", "sku", "price", "cost")) {
            context.append(buildProductContext(company));
        }

        if (containsAny(lowerQuestion, "supplier", "vendor", "contact", "supply", "source")) {
            context.append(buildSupplierContext(company));
        }

        if (containsAny(lowerQuestion, "category", "categories", "type", "group")) {
            context.append(buildCategoryContext(company));
        }

        if (containsAny(lowerQuestion, "transaction", "sale", "purchase", "sold", "bought", "history")) {
            context.append(buildTransactionContext(company));
        }

        // If no specific context matched, include all basic context
        if (context.toString().equals("=== CURRENT INVENTORY DATA ===\n\n" + buildSummaryStats(company))) {
            context.append(buildInventoryContext(company));
            context.append(buildProductContext(company));
            context.append(buildSupplierContext(company));
        }

        return context.toString();
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String buildSummaryStats(Company company) {
        List<Product> products = productService.getAllProducts(company);
        List<Supplier> suppliers = supplierService.getAllSuppliers(company);
        List<Category> categories = categoryService.getAllCategories(company);
        List<Inventory> inventories = inventoryService.getAllInventory(company);
        List<Inventory> lowStock = inventoryService.getLowStockItems(company);

        BigDecimal totalValue = inventories.stream()
                .map(inv -> inv.getProduct().getPrice().multiply(BigDecimal.valueOf(inv.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalQuantity = inventories.stream()
                .mapToInt(Inventory::getQuantity)
                .sum();

        return String.format("""
            SUMMARY:
            - Total Products: %d
            - Total Suppliers: %d
            - Total Categories: %d
            - Total Items in Stock: %d
            - Low Stock Items: %d
            - Total Inventory Value: $%.2f

            """,
            products.size(),
            suppliers.size(),
            categories.size(),
            totalQuantity,
            lowStock.size(),
            totalValue);
    }

    private String buildInventoryContext(Company company) {
        List<Inventory> inventories = inventoryService.getAllInventory(company);
        List<Inventory> lowStock = inventoryService.getLowStockItems(company);

        StringBuilder sb = new StringBuilder();
        sb.append("INVENTORY STATUS:\n");

        if (!lowStock.isEmpty()) {
            sb.append("\nLOW STOCK ALERTS:\n");
            for (Inventory inv : lowStock) {
                sb.append(String.format("- %s (SKU: %s): %d units (Reorder Level: %d, Reorder Qty: %d)\n",
                        inv.getProduct().getName(),
                        inv.getProduct().getSku(),
                        inv.getQuantity(),
                        inv.getReorderLevel(),
                        inv.getReorderQuantity()));
            }
        }

        sb.append("\nALL INVENTORY:\n");
        for (Inventory inv : inventories) {
            String status = inv.getQuantity() <= inv.getReorderLevel() ? " [LOW]" : "";
            sb.append(String.format("- %s: %d units at %s%s\n",
                    inv.getProduct().getName(),
                    inv.getQuantity(),
                    inv.getLocation() != null ? inv.getLocation() : "N/A",
                    status));
        }
        sb.append("\n");

        return sb.toString();
    }

    private String buildProductContext(Company company) {
        List<Product> products = productService.getAllProducts(company);

        StringBuilder sb = new StringBuilder();
        sb.append("PRODUCTS:\n");

        for (Product product : products) {
            sb.append(String.format("- %s (SKU: %s)\n  Price: $%.2f | Category: %s | Supplier: %s\n",
                    product.getName(),
                    product.getSku(),
                    product.getPrice(),
                    product.getCategory() != null ? product.getCategory().getName() : "N/A",
                    product.getSupplier() != null ? product.getSupplier().getName() : "N/A"));
            if (product.getDescription() != null && !product.getDescription().isEmpty()) {
                sb.append(String.format("  Description: %s\n", product.getDescription()));
            }
        }
        sb.append("\n");

        return sb.toString();
    }

    private String buildSupplierContext(Company company) {
        List<Supplier> suppliers = supplierService.getAllSuppliers(company);
        List<Product> products = productService.getAllProducts(company);

        StringBuilder sb = new StringBuilder();
        sb.append("SUPPLIERS:\n");

        for (Supplier supplier : suppliers) {
            long productCount = products.stream()
                    .filter(p -> p.getSupplier() != null && p.getSupplier().getId().equals(supplier.getId()))
                    .count();

            sb.append(String.format("- %s\n", supplier.getName()));
            sb.append(String.format("  Contact: %s | Email: %s | Phone: %s\n",
                    supplier.getContactName() != null ? supplier.getContactName() : "N/A",
                    supplier.getEmail() != null ? supplier.getEmail() : "N/A",
                    supplier.getPhone() != null ? supplier.getPhone() : "N/A"));
            sb.append(String.format("  Address: %s | Products Supplied: %d\n",
                    supplier.getAddress() != null ? supplier.getAddress() : "N/A",
                    productCount));

            // List products from this supplier
            List<String> supplierProducts = products.stream()
                    .filter(p -> p.getSupplier() != null && p.getSupplier().getId().equals(supplier.getId()))
                    .map(Product::getName)
                    .collect(Collectors.toList());
            if (!supplierProducts.isEmpty()) {
                sb.append(String.format("  Products: %s\n", String.join(", ", supplierProducts)));
            }
        }
        sb.append("\n");

        return sb.toString();
    }

    private String buildCategoryContext(Company company) {
        List<Category> categories = categoryService.getAllCategories(company);
        List<Product> products = productService.getAllProducts(company);

        StringBuilder sb = new StringBuilder();
        sb.append("CATEGORIES:\n");

        for (Category category : categories) {
            long productCount = products.stream()
                    .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(category.getId()))
                    .count();

            sb.append(String.format("- %s: %d products\n", category.getName(), productCount));
            if (category.getDescription() != null && !category.getDescription().isEmpty()) {
                sb.append(String.format("  Description: %s\n", category.getDescription()));
            }
        }
        sb.append("\n");

        return sb.toString();
    }

    private String buildTransactionContext(Company company) {
        List<Transaction> transactions = transactionService.getAllTransactions(company);

        StringBuilder sb = new StringBuilder();
        sb.append("RECENT TRANSACTIONS (Last 20):\n");

        List<Transaction> recentTransactions = transactions.stream()
                .sorted((t1, t2) -> t2.getTransactionDate().compareTo(t1.getTransactionDate()))
                .limit(20)
                .collect(Collectors.toList());

        for (Transaction tx : recentTransactions) {
            sb.append(String.format("- [%s] %s: %d units of %s @ $%.2f = $%.2f\n",
                    tx.getTransactionDate().toLocalDate(),
                    tx.getType(),
                    tx.getQuantity(),
                    tx.getProduct() != null ? tx.getProduct().getName() : "N/A",
                    tx.getUnitPrice(),
                    tx.getTotalAmount()));
            if (tx.getNotes() != null && !tx.getNotes().isEmpty()) {
                sb.append(String.format("  Notes: %s\n", tx.getNotes()));
            }
        }

        // Add transaction summary
        BigDecimal totalSales = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.SALE)
                .map(Transaction::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPurchases = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.PURCHASE)
                .map(Transaction::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        sb.append(String.format("\nTRANSACTION TOTALS:\n- Total Sales: $%.2f\n- Total Purchases: $%.2f\n\n",
                totalSales, totalPurchases));

        return sb.toString();
    }
}
