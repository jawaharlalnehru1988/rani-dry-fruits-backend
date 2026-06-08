package com.asknehru.myclientsapi.dresscatelog;

import com.asknehru.myclientsapi.core.exception.ResourceNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return toResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductWriteRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setImageUrl(request.getImageUrl());
        product.setProdDesc(request.getProdDesc());
        product.setIsNew(request.getIsNew() != null ? request.getIsNew() : false);

        if (request.getSizeWisePrice() != null) {
            Product finalProduct = product;
            List<ProductSizePrice> sizes = request.getSizeWisePrice().stream().map(input -> {
                ProductSizePrice sizePrice = new ProductSizePrice();
                sizePrice.setProduct(finalProduct);
                sizePrice.setSize(input.getSize());
                sizePrice.setOriginalPrice(input.getOriginalPrice());
                sizePrice.setDiscountedPrice(input.getDiscountedPrice());
                return sizePrice;
            }).collect(Collectors.toList());
            product.setSizeWisePrice(sizes);
        }

        product = productRepository.save(product);
        return toResponse(product);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductWriteRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (request.getName() != null) product.setName(request.getName());
        if (request.getCategory() != null) product.setCategory(request.getCategory());
        if (request.getImageUrl() != null) product.setImageUrl(request.getImageUrl());
        if (request.getProdDesc() != null) product.setProdDesc(request.getProdDesc());
        if (request.getIsNew() != null) product.setIsNew(request.getIsNew());

        if (request.getSizeWisePrice() != null) {
            product.getSizeWisePrice().clear();
            Product finalProduct = product;
            List<ProductSizePrice> sizes = request.getSizeWisePrice().stream().map(input -> {
                ProductSizePrice sizePrice = new ProductSizePrice();
                sizePrice.setProduct(finalProduct);
                sizePrice.setSize(input.getSize());
                sizePrice.setOriginalPrice(input.getOriginalPrice());
                sizePrice.setDiscountedPrice(input.getDiscountedPrice());
                return sizePrice;
            }).collect(Collectors.toList());
            product.getSizeWisePrice().addAll(sizes);
        }

        product = productRepository.save(product);
        return toResponse(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        productRepository.delete(product);
    }

    private ProductResponse toResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setCategory(product.getCategory());
        response.setImageUrl(product.getImageUrl());
        response.setProdDesc(product.getProdDesc());
        response.setIsNew(product.getIsNew());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        if (product.getSizeWisePrice() != null) {
            response.setSizeWisePrice(product.getSizeWisePrice().stream().map(sp -> {
                ProductResponse.SizeWisePriceDto dto = new ProductResponse.SizeWisePriceDto();
                dto.setSize(sp.getSize());
                dto.setOriginalPrice(sp.getOriginalPrice());
                dto.setDiscountedPrice(sp.getDiscountedPrice());
                return dto;
            }).collect(Collectors.toList()));
        }
        return response;
    }
}
