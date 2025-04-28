package com.example.usermanagementbackend.controller;

import com.example.usermanagementbackend.entity.Commande;
import com.example.usermanagementbackend.service.CommandeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/commandes")
public class CommandeController {

    private final CommandeService commandeService;

    public CommandeController(CommandeService commandeService) {
        this.commandeService = commandeService;
    }

    @GetMapping
    public ResponseEntity<?> getAllCommandes() {
        try {
            List<Commande> commandes = commandeService.getAllCommandes();
            return ResponseEntity.ok(commandes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching commandes: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCommandeById(@PathVariable Long id) {
        try {
            Optional<Commande> commande = commandeService.getCommandeById(id);
            return commande.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching commande with id " + id + ": " + e.getMessage());
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getCommandesByStatus(@PathVariable Commande.OrderStatus status) {
        try {
            List<Commande> commandes = commandeService.getCommandesByStatus(status);
            return ResponseEntity.ok(commandes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching commandes with status " + status + ": " + e.getMessage());
        }
    }

    @GetMapping("/date-range")
    public ResponseEntity<?> getCommandesByDateRange(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        try {
            List<Commande> commandes = commandeService.getCommandesByDateRange(startDate, endDate);
            return ResponseEntity.ok(commandes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching commandes between " + startDate + " and " + endDate + ": " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Commande>> getCommandesByUser(@PathVariable Long userId) {
        try {
            List<Commande> commandes = commandeService.getCommandesByUser(userId);
            return ResponseEntity.ok(commandes != null ? commandes : Collections.emptyList());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Collections.emptyList()); // Always return an empty array on error
        }
    }

    @GetMapping("/user/{userId}/pending")
    public ResponseEntity<?> getPendingCommandesByUser(@PathVariable Long userId) {
        try {
            List<Commande> commandes = commandeService.getPendingCommandesByUser(userId);
            return ResponseEntity.ok(commandes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching pending commandes for user " + userId + ": " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createCommande(@Valid @RequestBody Commande commande) {
        try {
            Commande savedCommande = commandeService.saveCommande(commande);
            return new ResponseEntity<>(savedCommande, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Validation error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error creating commande: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCommande(@PathVariable Long id, @RequestBody Commande commande) {
        try {
            Commande updatedCommande = commandeService.updateCommande(id, commande);
            return ResponseEntity.ok(updatedCommande);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating commande with id " + id + ": " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCommande(@PathVariable Long id) {
        try {
            commandeService.deleteCommande(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting commande with id " + id + ": " + e.getMessage());
        }
    }

    @PostMapping("/{id}/checkout")
    public ResponseEntity<?> checkoutCommande(@PathVariable Long id) {
        try {
            Commande commande = commandeService.getCommandeById(id)
                    .orElseThrow(() -> new RuntimeException("Commande non trouvée avec l'ID: " + id));
            if (commande.getStatus() != Commande.OrderStatus.PENDING && commande.getStatus() != Commande.OrderStatus.PENDING_PAYMENT) {
                throw new IllegalStateException("Commande must be in PENDING or PENDING_PAYMENT status to checkout");
            }
            commandeService.transitionOrderStatus(commande, Commande.OrderStatus.PAID);
            return ResponseEntity.ok(commande);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error checking out commande with id " + id + ": " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}