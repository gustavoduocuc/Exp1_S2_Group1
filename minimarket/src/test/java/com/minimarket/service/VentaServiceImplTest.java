package com.minimarket.service;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Venta;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.impl.VentaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VentaServiceImplTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private ProductoRepository productoRepository;

    private VentaServiceImpl ventaService;

    @BeforeEach
    void setUp() {
        ventaService = new VentaServiceImpl(ventaRepository, productoRepository);
    }

    @Test
    void rejectsSaleWhenProductStockIsInsufficient() {
        Producto producto = buildProducto(1L, "Arroz", 5);
        DetalleVenta detalle = buildDetalle(producto, 10);
        Venta venta = buildVenta(List.of(detalle));

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        assertThrows(IllegalStateException.class, () -> ventaService.save(venta));

        verifyNothingPersisted();
    }

    @Test
    void rejectsSaleWhenProductNotFound() {
        Producto producto = buildProducto(99L, "Inexistente", 10);
        DetalleVenta detalle = buildDetalle(producto, 1);
        Venta venta = buildVenta(List.of(detalle));

        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> ventaService.save(venta));

        verifyNothingPersisted();
    }

    @Test
    void savesSaleAndDeductsStockWhenStockIsSufficient() {
        Producto producto = buildProducto(1L, "Arroz", 10);
        DetalleVenta detalle = buildDetalle(producto, 3);
        Venta venta = buildVenta(List.of(detalle));

        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        whenProductoSavedReturnsFirstArg();
        whenVentaSaved(venta);

        ventaService.save(venta);

        ArgumentCaptor<Producto> productoCaptor = ArgumentCaptor.forClass(Producto.class);
        Producto savedProducto = verifyProductoSaved(productoCaptor);
        assertEquals(7, savedProducto.getStock());

        verifyVentaSaved(venta);
    }

    @Test
    void validatesStockForAllDetailsInSale() {
        Producto productoConStock = buildProducto(1L, "Arroz", 10);
        Producto productoSinStock = buildProducto(2L, "Aceite", 1);

        DetalleVenta detalleConStock = buildDetalle(productoConStock, 2);
        DetalleVenta detalleSinStock = buildDetalle(productoSinStock, 5);
        Venta venta = buildVenta(Arrays.asList(detalleConStock, detalleSinStock));

        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoConStock));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(productoSinStock));

        assertThrows(IllegalStateException.class, () -> ventaService.save(venta));

        verifyNothingPersisted();
    }

    private Producto buildProducto(Long id, String nombre, int stock) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre(nombre);
        producto.setStock(stock);
        producto.setPrecio(100.0);
        return producto;
    }

    private DetalleVenta buildDetalle(Producto producto, int cantidad) {
        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setPrecio(producto.getPrecio());
        return detalle;
    }

    private Venta buildVenta(List<DetalleVenta> detalles) {
        Venta venta = new Venta();
        venta.setDetalles(detalles);
        return venta;
    }

    @SuppressWarnings("null")
    private void verifyNothingPersisted() {
        verify(productoRepository, never()).save(notNull(Producto.class));
        verify(ventaRepository, never()).save(notNull(Venta.class));
    }

    @SuppressWarnings("null")
    private void whenProductoSavedReturnsFirstArg() {
        when(productoRepository.save(notNull(Producto.class))).thenAnswer(
                invocation -> Objects.requireNonNull(invocation.getArgument(0, Producto.class)));
    }

    @SuppressWarnings("null")
    private void whenVentaSaved(Venta venta) {
        Venta nonNullVenta = Objects.requireNonNull(venta);
        when(ventaRepository.save(nonNullVenta)).thenReturn(nonNullVenta);
    }

    @SuppressWarnings("null")
    private Producto verifyProductoSaved(ArgumentCaptor<Producto> captor) {
        verify(productoRepository).save(captor.capture());
        return Objects.requireNonNull(captor.getValue());
    }

    @SuppressWarnings("null")
    private void verifyVentaSaved(Venta venta) {
        verify(ventaRepository).save(Objects.requireNonNull(venta));
    }
}
