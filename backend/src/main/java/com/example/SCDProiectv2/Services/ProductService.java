package com.example.SCDProiectv2.Services;

import com.example.SCDProiectv2.DTOs.ProductDTO;
import com.example.SCDProiectv2.Models.Courier;
import com.example.SCDProiectv2.Models.Product;
import com.example.SCDProiectv2.Repositories.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public ResponseEntity<?> createProduct(ProductDTO productDTO) {
        try{
            Product product = new Product();
            product.setName(productDTO.getProductName());
            product.setDescription(productDTO.getProductDescription());
            product.setPrice(productDTO.getProductPrice());
            productRepository.save(product);
            return ResponseEntity.status(HttpStatus.CREATED).body("Product created successfully");
        }
        catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    @Transactional
    public ResponseEntity<?> deleteProduct(String productName) {
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String managerName = auth.getName();
            Optional<Product> product = productRepository.findByName(productName);
            if(product.isPresent()) {
                productRepository.delete(product.get());
                return ResponseEntity.status(HttpStatus.OK).body("Product deleted successfully");
            }
            else
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");

        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    @Transactional
    public ResponseEntity<?> updateProduct(ProductDTO productDTO) {
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String managerName = auth.getName();
            Optional<Product> product = productRepository.findByName(productDTO.getProductName());
            if(product.isPresent()) {
                if(productDTO.getProductDescription() != null) {
                    product.get().setDescription(productDTO.getProductDescription());
                }
                if(productDTO.getProductPrice() != null) {
                    product.get().setPrice(productDTO.getProductPrice());
                }
                productRepository.save(product.get());
                return ResponseEntity.status(HttpStatus.OK).body("Product updated successfully");
            }
            else
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");

        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    public ResponseEntity<?> getAllProducts() {
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String managerName = auth.getName();
            List<Product> products = productRepository.findAll();
            return ResponseEntity.status(HttpStatus.OK).body(products);
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
