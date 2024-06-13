package springboot.curd.beststore.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springboot.curd.beststore.models.Product;
import springboot.curd.beststore.models.ProductDto;
import springboot.curd.beststore.services.ProductsRepository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductsController {
    private ProductsRepository repo;


    @Autowired
    public ProductsController(ProductsRepository productsRepository) {
        this.repo = productsRepository;
    }

    @GetMapping({"", "/"})
    public String showProductList(Model model) {
        List<Product> products = repo.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("products", products);
        return "products/index";
    }

    @GetMapping("/create")
    public String showCreatePage(Model model) {
        ProductDto productDto = new ProductDto();
        model.addAttribute("productDto", productDto);
        return "products/createProduct";
    }

    @PostMapping("create")
    // @Valid to gather the attributes filled out in the form
    // BindingResult: object so that you can test for and retrieve validation errors
    public String createProduct(@Valid @ModelAttribute ProductDto productDto,
                                BindingResult bindingResult) {

        // Kiem tra xem anh co trong khong. Neu trong thi se hien thi loi
        if (productDto.getImageFile().isEmpty()) {
            bindingResult.addError(new FieldError("productDto", "imageFile", "Image file is required"));
        }

        // Kiem tra xem co loi khong neu co se tra ve form createProduct va hien thi loi ra mang hinh
        if (bindingResult.hasErrors()) {
            return "products/createProduct";
        }

        // Save image file
        MultipartFile image = productDto.getImageFile();
        Date createAt = new Date();
        String storageFileName = createAt.getTime() + "_" + image.getOriginalFilename();

        try {
            String uploadDir = "public/images/";
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            System.out.println("Exception: " + e.getMessage());
        }

        Product product = new Product();
        product.setName(productDto.getName());
        product.setBrand(productDto.getBrand());
        product.setCategory(productDto.getCategory());
        product.setPrice(productDto.getPrice());
        product.setDescription(productDto.getDescription());
        product.setCreatedAt(createAt);
        product.setImageFileName(storageFileName);

        this.repo.save(product);

        return "redirect:/products";
    }

    @GetMapping("/edit")
    public String showEditPage(Model model,
                               @RequestParam int id) {
        try {
            Product product = repo.findById(id).get();
            model.addAttribute("product", product);

            ProductDto productDto = new ProductDto();
            productDto.setName(product.getName());
            productDto.setBrand(product.getBrand());
            productDto.setCategory(product.getCategory());
            productDto.setPrice(product.getPrice());
            product.setDescription(product.getDescription());

            model.addAttribute("productDto", productDto);
        } catch (Exception ex) {
            System.out.println("Exception edit: " + ex.getMessage());
            return "redirect:/products";
        }

        return "products/EditProduct";
    }

    @PostMapping("/edit")
    public String updateProduct(Model model,
                                @RequestParam int id,
                                @Valid @ModelAttribute ProductDto productDto,
                                BindingResult result,
                                HttpServletRequest request) {


        try {
            Product product = repo.findById(id).get();
            model.addAttribute("product", product);

            if (result.hasErrors()) {
                return "products/EditProducts";
            }

            if (!product.getImageFileName().isEmpty()) {
                // delete old image
                String uploadDir = "public/images/";
                Path oldImagePath = Paths.get(uploadDir + product.getImageFileName());

                try {
                    Files.delete(oldImagePath);
                } catch (Exception ex) {
                    System.out.println("Exception: " + ex.getMessage());
                }

                // save new image file
                MultipartFile image = productDto.getImageFile();
                Date createAt = new Date();
                String storageFileName = createAt.getTime() + "_" + image.getOriginalFilename();

                try (InputStream inputStream = image.getInputStream()) {
                    Files.copy(inputStream, Paths.get(uploadDir + storageFileName),
                            StandardCopyOption.REPLACE_EXISTING);
                }

                product.setImageFileName(storageFileName);
            }

            product.setName(productDto.getName());
            product.setBrand(productDto.getBrand());
            product.setCategory(productDto.getCategory());
            product.setPrice(productDto.getPrice());
            product.setDescription(productDto.getDescription());

            repo.save(product);
        } catch (Exception ex) {
            System.out.println("Exception update: " + ex.getMessage());
        }

        return "redirect:/products";
    }

    @GetMapping("/delete")
    public String deleteProduct(@RequestParam int id) {
        try {
            Product product = repo.findById(id).get();

            // Delete product image
            Path imgagePath = Paths.get("public/images/" + product.getImageFileName());

            try {
                Files.delete(imgagePath);
            } catch (Exception ex) {
                System.out.println("Ex: " + ex.getMessage());
            }

            // delete the product
            repo.delete(product);
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
        return "redirect:/products";
    }
}
