package com.example.inventory_backend.service;

import com.example.inventory_backend.model.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExportService {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private InventoryService inventoryService;

    public byte[] exportAllToExcel(Company company) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); 
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // Products Sheet
            List<Product> products = productService.getAllProducts(company);
            Sheet productsSheet = workbook.createSheet("Products");
            Row productHeader = productsSheet.createRow(0);
            String[] productColumns = {"ID", "Name", "Description", "SKU", "Price", "Category", "Supplier"};
            for (int i = 0; i < productColumns.length; i++) {
                Cell cell = productHeader.createCell(i);
                cell.setCellValue(productColumns[i]);
                cell.setCellStyle(headerStyle);
            }
            int rowNum = 1;
            for (Product product : products) {
                Row row = productsSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(product.getId());
                row.createCell(1).setCellValue(product.getName());
                row.createCell(2).setCellValue(product.getDescription() != null ? product.getDescription() : "");
                row.createCell(3).setCellValue(product.getSku() != null ? product.getSku() : "");
                row.createCell(4).setCellValue(product.getPrice() != null ? product.getPrice().doubleValue() : 0.0);
                row.createCell(5).setCellValue(product.getCategory() != null ? product.getCategory().getName() : "");
                row.createCell(6).setCellValue(product.getSupplier() != null ? product.getSupplier().getName() : "");
            }
            for (int i = 0; i < productColumns.length; i++) {
                productsSheet.autoSizeColumn(i);
            }
            
            // Categories Sheet
            List<Category> categories = categoryService.getAllCategories(company);
            Sheet categoriesSheet = workbook.createSheet("Categories");
            Row categoryHeader = categoriesSheet.createRow(0);
            String[] categoryColumns = {"ID", "Name", "Description", "Product Count"};
            for (int i = 0; i < categoryColumns.length; i++) {
                Cell cell = categoryHeader.createCell(i);
                cell.setCellValue(categoryColumns[i]);
                cell.setCellStyle(headerStyle);
            }
            rowNum = 1;
            for (Category category : categories) {
                Row row = categoriesSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(category.getId());
                row.createCell(1).setCellValue(category.getName());
                row.createCell(2).setCellValue(category.getDescription() != null ? category.getDescription() : "");
                row.createCell(3).setCellValue(category.getProducts() != null ? category.getProducts().size() : 0);
            }
            for (int i = 0; i < categoryColumns.length; i++) {
                categoriesSheet.autoSizeColumn(i);
            }
            
            // Suppliers Sheet
            List<Supplier> suppliers = supplierService.getAllSuppliers(company);
            Sheet suppliersSheet = workbook.createSheet("Suppliers");
            Row supplierHeader = suppliersSheet.createRow(0);
            String[] supplierColumns = {"ID", "Name", "Contact Name", "Email", "Phone", "Address"};
            for (int i = 0; i < supplierColumns.length; i++) {
                Cell cell = supplierHeader.createCell(i);
                cell.setCellValue(supplierColumns[i]);
                cell.setCellStyle(headerStyle);
            }
            rowNum = 1;
            for (Supplier supplier : suppliers) {
                Row row = suppliersSheet.createRow(rowNum++);
                row.createCell(0).setCellValue(supplier.getId());
                row.createCell(1).setCellValue(supplier.getName());
                row.createCell(2).setCellValue(supplier.getContactName() != null ? supplier.getContactName() : "");
                row.createCell(3).setCellValue(supplier.getEmail() != null ? supplier.getEmail() : "");
                row.createCell(4).setCellValue(supplier.getPhone() != null ? supplier.getPhone() : "");
                row.createCell(5).setCellValue(supplier.getAddress() != null ? supplier.getAddress() : "");
            }
            for (int i = 0; i < supplierColumns.length; i++) {
                suppliersSheet.autoSizeColumn(i);
            }
            
            // Inventory Sheet
            List<Inventory> inventoryList = inventoryService.getAllInventory(company);
            Sheet inventorySheet = workbook.createSheet("Inventory");
            Row inventoryHeader = inventorySheet.createRow(0);
            String[] inventoryColumns = {"ID", "Product", "SKU", "Quantity", "Reorder Level", "Reorder Quantity", "Location", "Last Updated"};
            for (int i = 0; i < inventoryColumns.length; i++) {
                Cell cell = inventoryHeader.createCell(i);
                cell.setCellValue(inventoryColumns[i]);
                cell.setCellStyle(headerStyle);
            }
            rowNum = 1;
            for (Inventory inventory : inventoryList) {
                Row row = inventorySheet.createRow(rowNum++);
                row.createCell(0).setCellValue(inventory.getId());
                row.createCell(1).setCellValue(inventory.getProduct() != null ? inventory.getProduct().getName() : "");
                row.createCell(2).setCellValue(inventory.getProduct() != null ? inventory.getProduct().getSku() : "");
                row.createCell(3).setCellValue(inventory.getQuantity() != null ? inventory.getQuantity() : 0);
                row.createCell(4).setCellValue(inventory.getReorderLevel() != null ? inventory.getReorderLevel() : 0);
                row.createCell(5).setCellValue(inventory.getReorderQuantity() != null ? inventory.getReorderQuantity() : 0);
                row.createCell(6).setCellValue(inventory.getLocation() != null ? inventory.getLocation() : "");
                row.createCell(7).setCellValue(inventory.getLastUpdated() != null ? inventory.getLastUpdated().toString() : "");
            }
            for (int i = 0; i < inventoryColumns.length; i++) {
                inventorySheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportProductsToExcel(Company company) throws IOException {
        List<Product> products = productService.getAllProducts(company);
        
        try (Workbook workbook = new XSSFWorkbook(); 
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Products");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Name", "Description", "SKU", "Price", "Category", "Supplier"};
            
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Product product : products) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(product.getId());
                row.createCell(1).setCellValue(product.getName());
                row.createCell(2).setCellValue(product.getDescription() != null ? product.getDescription() : "");
                row.createCell(3).setCellValue(product.getSku() != null ? product.getSku() : "");
                row.createCell(4).setCellValue(product.getPrice() != null ? product.getPrice().doubleValue() : 0.0);
                row.createCell(5).setCellValue(product.getCategory() != null ? product.getCategory().getName() : "");
                row.createCell(6).setCellValue(product.getSupplier() != null ? product.getSupplier().getName() : "");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportSuppliersToExcel(Company company) throws IOException {
        List<Supplier> suppliers = supplierService.getAllSuppliers(company);
        
        try (Workbook workbook = new XSSFWorkbook(); 
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Suppliers");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Name", "Contact Name", "Email", "Phone", "Address"};
            
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Supplier supplier : suppliers) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(supplier.getId());
                row.createCell(1).setCellValue(supplier.getName());
                row.createCell(2).setCellValue(supplier.getContactName() != null ? supplier.getContactName() : "");
                row.createCell(3).setCellValue(supplier.getEmail() != null ? supplier.getEmail() : "");
                row.createCell(4).setCellValue(supplier.getPhone() != null ? supplier.getPhone() : "");
                row.createCell(5).setCellValue(supplier.getAddress() != null ? supplier.getAddress() : "");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportInventoryToExcel(Company company) throws IOException {
        List<Inventory> inventoryList = inventoryService.getAllInventory(company);
        
        try (Workbook workbook = new XSSFWorkbook(); 
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Inventory");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Product", "SKU", "Quantity", "Reorder Level", "Reorder Quantity", "Location", "Last Updated"};
            
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Inventory inventory : inventoryList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(inventory.getId());
                row.createCell(1).setCellValue(inventory.getProduct() != null ? inventory.getProduct().getName() : "");
                row.createCell(2).setCellValue(inventory.getProduct() != null ? inventory.getProduct().getSku() : "");
                row.createCell(3).setCellValue(inventory.getQuantity() != null ? inventory.getQuantity() : 0);
                row.createCell(4).setCellValue(inventory.getReorderLevel() != null ? inventory.getReorderLevel() : 0);
                row.createCell(5).setCellValue(inventory.getReorderQuantity() != null ? inventory.getReorderQuantity() : 0);
                row.createCell(6).setCellValue(inventory.getLocation() != null ? inventory.getLocation() : "");
                row.createCell(7).setCellValue(inventory.getLastUpdated() != null ? inventory.getLastUpdated().toString() : "");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] exportCategoriesToExcel(Company company) throws IOException {
        List<Category> categories = categoryService.getAllCategories(company);
        
        try (Workbook workbook = new XSSFWorkbook(); 
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Categories");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Name", "Description", "Product Count"};
            
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Category category : categories) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(category.getId());
                row.createCell(1).setCellValue(category.getName());
                row.createCell(2).setCellValue(category.getDescription() != null ? category.getDescription() : "");
                row.createCell(3).setCellValue(category.getProducts() != null ? category.getProducts().size() : 0);
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}