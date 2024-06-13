package springboot.curd.beststore.services;

import org.springframework.data.jpa.repository.JpaRepository;
import springboot.curd.beststore.models.Product;

// JpaRepository se cung cap cac basic CRUD khi thao tac voi entity or class
public interface ProductsRepository extends JpaRepository<Product, Integer> {
}
