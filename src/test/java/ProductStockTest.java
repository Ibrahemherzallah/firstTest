import org.example.ProductStock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProductStock — full behavior and validation tests")
class ProductStockTest {

    // -------------------------
    // Constructor + getters
    // -------------------------
    @Test
    @DisplayName("Constructor assigns fields and getters return correct values")
    void constructorAssignsValues() {
        ProductStock ps = new ProductStock("SKU-1", "WH-1-A3", 10, 5, 100);
        assertEquals("SKU-1", ps.getProductId());
        assertEquals("WH-1-A3", ps.getLocation());
        assertEquals(10, ps.getOnHand());
        assertEquals(0, ps.getReserved());
        assertEquals(5, ps.getReorderThreshold());
        assertEquals(100, ps.getMaxCapacity());
        assertEquals(10, ps.getAvailable());
        assertTrue(ps.toString().contains("SKU-1"));
        assertTrue(ps.toString().contains("WH-1-A3"));
    }

    @Test
    @DisplayName("Constructor rejects null/blank productId or location")
    void constructorRejectsBlankIdOrLocation() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new ProductStock(null, "L", 0, 0, 1)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new ProductStock("", "L", 0, 0, 1)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new ProductStock("P", null, 0, 0, 1)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new ProductStock("P", "   ", 0, 0, 1))
        );
    }

    @Test
    @DisplayName("Constructor rejects negative or invalid numeric args")
    void constructorRejectsInvalidNumbers() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new ProductStock("P", "L", -1, 0, 1)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new ProductStock("P", "L", 0, -1, 1)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new ProductStock("P", "L", 0, 0, 0)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> new ProductStock("P", "L", 11, 0, 10)) // initialOnHand > maxCapacity
        );
    }

    // -------------------------
    // changeLocation
    // -------------------------
    @Test
    @DisplayName("changeLocation sets a new valid location")
    void changeLocationValid() {
        ProductStock ps = new ProductStock("P", "L1", 1, 0, 10);
        ps.changeLocation("L2");
        assertEquals("L2", ps.getLocation());
    }

    @Test
    @DisplayName("changeLocation rejects null/blank")
    void changeLocationInvalid() {
        ProductStock ps = new ProductStock("P", "L1", 1, 0, 10);
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> ps.changeLocation(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> ps.changeLocation(" "))
        );
    }

    // -------------------------
    // addStock
    // -------------------------
    @Test
    @DisplayName("addStock increases onHand and rejects invalid amounts or overflow")
    void addStockWorksAndRejects() {
        ProductStock ps = new ProductStock("P", "L", 5, 0, 10);
        ps.addStock(3);
        assertEquals(8, ps.getOnHand());

        // invalid amount
        assertThrows(IllegalArgumentException.class, () -> ps.addStock(0));
        assertThrows(IllegalArgumentException.class, () -> ps.addStock(-1));

        // overflow beyond maxCapacity
        assertThrows(IllegalStateException.class, () -> ps.addStock(5 + 10)); // would exceed 10
    }

    // -------------------------
    // removeDamaged
    // -------------------------
    @Test
    @DisplayName("removeDamaged decreases onHand and clamps reserved when necessary")
    void removeDamagedReducesAndClampsReserved() {
        ProductStock ps = new ProductStock("P", "L", 10, 0, 20);
        // reserve some then remove more than available after reservation clamp
        ps.reserve(6);            // reserved = 6, available = 4
        assertEquals(6, ps.getReserved());
        ps.removeDamaged(8);      // onHand = 2, reserved should be clamped to 2
        assertEquals(2, ps.getOnHand());
        assertEquals(2, ps.getReserved());

        // invalid removals
        assertThrows(IllegalArgumentException.class, () -> ps.removeDamaged(0));
        assertThrows(IllegalArgumentException.class, () -> ps.removeDamaged(-2));
        // cannot remove more than onHand
        assertThrows(IllegalStateException.class, () -> ps.removeDamaged(5)); // onHand is 2
    }

    // -------------------------
    // reserve and releaseReservation
    // -------------------------
    @Test
    @DisplayName("reserve and releaseReservation happy paths and validation")
    void reserveAndReleaseHappyAndInvalid() {
        ProductStock ps = new ProductStock("P", "L", 10, 0, 20);
        ps.reserve(4);
        assertEquals(4, ps.getReserved());
        assertEquals(6, ps.getAvailable());

        // invalid reserve amounts
        assertThrows(IllegalArgumentException.class, () -> ps.reserve(0));
        assertThrows(IllegalArgumentException.class, () -> ps.reserve(-1));
        // cannot reserve more than available
        assertThrows(IllegalStateException.class, () -> ps.reserve(7)); // available 6

        // release valid
        ps.releaseReservation(2);
        assertEquals(2, ps.getReserved());

        // invalid release
        assertThrows(IllegalArgumentException.class, () -> ps.releaseReservation(0));
        assertThrows(IllegalArgumentException.class, () -> ps.releaseReservation(-1));
        assertThrows(IllegalStateException.class, () -> ps.releaseReservation(5)); // only 2 reserved
    }

    // -------------------------
    // shipReserved
    // -------------------------
    @Test
    @DisplayName("shipReserved reduces reserved and onHand; validates inputs")
    void shipReservedBehavior() {
        ProductStock ps = new ProductStock("P", "L", 10, 0, 20);
        ps.reserve(5);
        assertEquals(5, ps.getReserved());
        ps.shipReserved(3);
        assertEquals(2, ps.getReserved());
        assertEquals(7, ps.getOnHand());

        // invalid shipments
        assertThrows(IllegalArgumentException.class, () -> ps.shipReserved(0));
        assertThrows(IllegalArgumentException.class, () -> ps.shipReserved(-1));
        // cannot ship more than reserved
        assertThrows(IllegalStateException.class, () -> ps.shipReserved(5)); // reserved=2
    }

    @Test
    @DisplayName("shipReserved guards against onHand shortage even if reserved invariant broken")
    void shipReservedGuardsOnHand() {
        // Create a situation where reserved <= onHand normally, then artificially adjust fields
        ProductStock ps = new ProductStock("P", "L", 2, 0, 10);
        // artificially simulate reserved > onHand by direct field access (allowed because fields are package-visible)
        ps.reserved = 3; // simulate corrupted state
        // shipReserved should detect amount > onHand
        assertThrows(IllegalStateException.class, () -> ps.shipReserved(2)); // amount <= reserved but > onHand
    }

    // -------------------------
    // isReorderNeeded + updateReorderThreshold
    // -------------------------
    @Test
    @DisplayName("isReorderNeeded based on available and threshold")
    void reorderNeededLogic() {
        ProductStock ps = new ProductStock("P", "L", 10, 5, 20);
        assertFalse(ps.isReorderNeeded()); // available 10 >= 5
        ps.reserve(7);                      // available 3
        assertTrue(ps.isReorderNeeded());  // 3 < 5
    }

    @Test
    @DisplayName("updateReorderThreshold validates bounds")
    void updateReorderThresholdValidation() {
        ProductStock ps = new ProductStock("P", "L", 1, 1, 5);
        // invalid negative
        assertThrows(IllegalArgumentException.class, () -> ps.updateReorderThreshold(-1));
        // invalid > maxCapacity
        assertThrows(IllegalArgumentException.class, () -> ps.updateReorderThreshold(10));
        // valid update
        ps.updateReorderThreshold(3);
        assertEquals(3, ps.getReorderThreshold());
    }

    // -------------------------
    // updateMaxCapacity
    // -------------------------
    @Test
    @DisplayName("updateMaxCapacity adjusts and enforces constraints")
    void updateMaxCapacityBehavior() {
        ProductStock ps = new ProductStock("P", "L", 5, 4, 10);
        // cannot set <= 0
        assertThrows(IllegalArgumentException.class, () -> ps.updateMaxCapacity(0));
        // cannot set less than onHand
        assertThrows(IllegalStateException.class, () -> ps.updateMaxCapacity(4));
        // when new maxCapacity is >= onHand, it should apply and possibly clamp reorderThreshold
        ps.updateMaxCapacity(6);
        assertEquals(6, ps.getMaxCapacity());
        // if reorderThreshold > new maxCapacity it gets clamped — set a larger threshold first
        ps.updateReorderThreshold(6);
        ps.updateMaxCapacity(5); // reorderThreshold (6) > new max 5 → should be clamped to 5
        assertEquals(5, ps.getMaxCapacity());
        assertEquals(5, ps.getReorderThreshold());
    }

    // -------------------------
    // combined scenario
    // -------------------------
    @Test
    @DisplayName("typical lifecycle: add, reserve, ship, removeDamaged")
    void lifecycleScenario() {
        ProductStock ps = new ProductStock("SKU-100", "LOC-1", 50, 10, 100);
        ps.addStock(20);            // 70
        ps.reserve(30);             // reserved=30 available=40
        assertEquals(40, ps.getAvailable());
        ps.shipReserved(20);        // reserved=10 onHand=50
        assertEquals(10, ps.getReserved());
        ps.removeDamaged(5);        // onHand=45 reserved still 10
        assertEquals(45, ps.getOnHand());
        assertEquals(10, ps.getReserved());
    }
}
