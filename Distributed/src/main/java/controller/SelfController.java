package controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/search")
public class SelfController {

    @Value("${file.search.directory}")
    private String searchDirectory;

    /**
     * Endpoint to check if a file exists in the configured directory
     * @param fileName the name of the file to search for
     * @return "yes" if the file exists, "no" if it doesn't
     */
    @GetMapping
    @JsonIgnore
    public String checkFileExists(@RequestParam(name = "filename") String fileName) {
        System.out.println("Controller method called. Searching for: " + fileName);
        System.out.println("Search directory: " + searchDirectory);
        try {
            String storagePath= System.getProperty("user.dir")+"/storage/";
            System.out.println("Full path being checked: " + storagePath.toString());



            java.io.File file=new java.io.File(storagePath+fileName);
            boolean exists=file.exists() && file.isFile();
            System.out.println("Peer check for file: "+storagePath+fileName+" - Exists: "+exists);
            return exists==true?"yes":"no";
        } catch (Exception e) {
            // Log the exception
            System.err.println("Error while checking file: " + e.getMessage());
            e.printStackTrace();
            return "no";
        }
    }
}