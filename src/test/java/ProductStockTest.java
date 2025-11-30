package org.example;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductStockTest {

    static ProductStock globalStock;
    ProductStock stock;


    @BeforeAll
    static void beforeAll() {
        System.out.println("Starting ProductStock Test Suite...");
        globalStock = new ProductStock(10, 100, 5);
    }

    @BeforeEach
    void beforeEach() {
        stock = new ProductStock(20, 100, 10);
    }

    @AfterEach
    void afterEach() {
        System.out.println("Finished a test.");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("ALL tests completed.");
    }


    @Test
    @Order(1)
    @Tag("sanity")
    @DisplayName("Constructor - Valid Parameters")
    void testConstructorValid() {
        ProductStock p = new ProductStock(10, 100, 10);
        assertAll(
                () -> assertEquals(10, p.stock),
                () -> assertEquals(100, p.maxCapacity),
                () -> assertEquals(10, p.reorderThreshold)
        );
    }

    @Test
    @Order(2)
    @Tag("regression")
    @DisplayName("Constructor - Invalid Parameters Throw Exception")
    void testConstructorInvalid() {
        assertThrows(IllegalArgumentException.class, () -> new ProductStock(-1, 100, 5));
        assertThrows(IllegalArgumentException.class, () -> new ProductStock(200, 100, 5));
        assertThrows(IllegalArgumentException.class, () -> new ProductStock(10, -5, 5));
        assertThrows(IllegalArgumentException.class, () -> new ProductStock(10, 100, -1));
    }


    @Test
    @Order(3)
    @DisplayName("Add valid stock amount")
    void testAddStockNormal() {
        stock.addStock(10);
        assertEquals(30, stock.stock);
    }

    @Test
    @Order(4)
    @DisplayName("Add stock exceeding capacity should throw")
    void testAddStockBeyondCapacity() {
        assertThrows(IllegalArgumentException.class, () -> stock.addStock(10000));
    }

    @Test
    @Order(5)
    @DisplayName("Add zero or negative stock should throw")
    void testAddStockInvalid() {
        assertThrows(IllegalArgumentException.class, () -> stock.addStock(0));
        assertThrows(IllegalArgumentException.class, () -> stock.addStock(-1));
    }

    @Test
    @Order(6)
    @DisplayName("Reserve valid amount")
    void testReserveNormal() {
        stock.reserve(10);
        assertEquals(10, stock.stock);
        assertEquals(10, stock.reserved);
    }

    @Test
    @Order(7)
    @DisplayName("Reserve more than available should throw")
    void testReserveTooMuch() {
        assertThrows(IllegalStateException.class, () -> stock.reserve(1000));
    }


    @Test
    @Order(8)
    @DisplayName("Release reserved amount correctly")
    void testReleaseReservation() {
        stock.reserve(10);
        stock.releaseReservation(5);

        assertEquals(5, stock.reserved);
        assertEquals(15, stock.stock);
    }

    @Test
    @Order(9)
    @DisplayName("Release more than reserved should throw")
    void testReleaseTooMuch() {
        stock.reserve(5);
        assertThrows(IllegalStateException.class, () -> stock.releaseReservation(10));
    }


    @Test
    @Order(10)
    @DisplayName("Ship reserved amount correctly")
    @Timeout(1)
    void testShipReserved() {
        stock.reserve(10);
        stock.shipReserved(10);

        assertEquals(0, stock.reserved);
        assertEquals(10, stock.stock);
    }

    @Test
    @Order(11)
    @DisplayName("Ship more than reserved should throw")
    void testShipReservedTooMuch() {
        assertThrows(IllegalStateException.class, () -> stock.shipReserved(10));
    }

    @Test
    @Order(12)
    @DisplayName("Remove damaged stock")
    void testRemoveDamaged() {
        stock.removeDamaged(5);
        assertEquals(15, stock.stock);
    }

    @Test
    @Order(13)
    @DisplayName("Remove more damaged than available throws")
    void testRemoveDamagedTooMuch() {
        assertThrows(IllegalStateException.class, () -> stock.removeDamaged(100));
    }

    @Test
    @Order(14)
    @DisplayName("Reorder needed when stock is below threshold")
    void testReorderNeeded() {
        ProductStock p = new ProductStock(5, 100, 10);
        assertTrue(p.isReorderNeeded());
    }

    @Test
    @Order(15)
    @DisplayName("Reorder NOT needed when stock is above threshold")
    void testReorderNotNeeded() {
        assertFalse(stock.isReorderNeeded());
    }

    @Test
    @Order(16)
    @DisplayName("Update reorder threshold")
    void testUpdateThreshold() {
        stock.updateReorderThreshold(2);
        assertEquals(2, stock.reorderThreshold);
    }

    @Test
    @Order(17)
    @DisplayName("Update max capacity")
    void testUpdateMaxCapacity() {
        stock.updateMaxCapacity(200);
        assertEquals(200, stock.maxCapacity);
    }

    @Test
    @Order(18)
    @DisplayName("Update max capacity smaller than stock throws")
    void testUpdateMaxCapacityInvalid() {
        assertThrows(IllegalStateException.class, () -> stock.updateMaxCapacity(5));
    }

    @Disabled("Feature not yet implemented")
    @Test
    void testFutureFeature() {
        fail("Not implemented.");
    }

}
