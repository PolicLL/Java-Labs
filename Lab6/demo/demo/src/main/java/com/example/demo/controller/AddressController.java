package com.example.demo.controller;

import com.example.demo.DTO.AddressDTO;
import com.example.demo.exception.AddressNotFoundException;
import com.example.demo.exception.ErrorResponse;
import com.example.demo.exception.InvalidInputException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Address;
import com.example.demo.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/address")
public class AddressController {

    private final AddressService addressService;

    @Autowired
    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    public ResponseEntity<?> createAddress(@RequestBody AddressDTO addressDTO) {
        try{
            Address createdAddress = addressService.createAddress(addressDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAddress);
        } catch (InvalidInputException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<List<Address>> getAddressList() {
        List<Address> addresses = addressService.getAddressList();
        if (addresses.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(addresses);
        else
            return ResponseEntity.ok(addresses);
    }

    // http://localhost:8080/address/test-pagination?page=0&size=3&sort=postalCode,asc

    @GetMapping("/test-pagination")
    public ResponseEntity<Page<Address>> getAddressList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort
    ) {
        Page<Address> addresses = getAddressPageWithSorting(page, size, sort);

        if (addresses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(addresses);
        } else {
            return ResponseEntity.ok(addresses);
        }
    }

    private Page<Address> getAddressPageWithSorting(int page, int size, String[] sort) {
        Sort.Direction direction = Sort.Direction.ASC;
        String sortBy = "id"; // Default sorting by ID and ascending

        if (sort.length > 0) {
            sortBy = sort[0]; // First element is the field name
            if (sort.length > 1) {
                direction = sort[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            }
        }

        return addressService.getAddressPage(
                PageRequest.of(page, size, Sort.by(direction, sortBy))
        );
    }


    @GetMapping("/{addressId}")
    public ResponseEntity<Address> getAddress(@PathVariable UUID addressId) {
        Optional<Address> address = addressService.getAddressById(addressId);

        if (address.isEmpty()) {
            String message = "ERROR CODE 404 \n" + "Address with ID " + addressId + " not found";
            throw new NotFoundException(message);
        }

        return address.map(tempAddress -> ResponseEntity.ok().body(tempAddress))
                            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .build());
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<?> updateAddress(@PathVariable UUID addressId, @RequestBody AddressDTO addressDTO) {
        try{
            Address updatedAddress = addressService.updateAddress(addressId, addressDTO);
            return ResponseEntity.ok(updatedAddress);
        }
        catch (AddressNotFoundException addressNotFoundException){
            ErrorResponse errorResponse = new ErrorResponse(addressNotFoundException.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<?> deleteAddress(@PathVariable UUID addressId) {
        try{
            addressService.deleteAddress(addressId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        catch (AddressNotFoundException addressNotFoundException){
            ErrorResponse errorResponse = new ErrorResponse(addressNotFoundException.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}