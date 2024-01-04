package cl.cvega.android.evaluacion_u_2.ui.theme.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.cvega.android.evaluacion_u_2.data.ComprasRepository
import cl.cvega.android.evaluacion_u_2.data.modelo.Compra
import cl.cvega.android.evaluacion_u_2.ui.state.ComprasUIState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID

class ComprasViewModel(
    private val comprasRepository: ComprasRepository = ComprasRepository()
) : ViewModel() {

    companion object{
        const val FILE_NAME = "compras.data"
    }

    private var job: Job? = null

    //deja la version mutable como privada
    private val _uiState = MutableStateFlow(ComprasUIState())
    // y publica la version solo lectura
    // asi aseguramos que solo el viewModel pueda modificar
    val uiState:StateFlow<ComprasUIState> = _uiState.asStateFlow()

    init {
        obtenerCompras()
    }

    fun obtenerComprasGuardadasEnDisco(fileInputStream: FileInputStream) {
        comprasRepository.getComprasEnDisco(fileInputStream)
    }

    fun guardarComprasEnDisco(fileOutputStream: FileOutputStream) {
        comprasRepository.guardarComprasEnDisco(fileOutputStream)
    }

    private fun obtenerCompras() {
        job?.cancel()
        job = viewModelScope.launch {
            val comprasStream = comprasRepository.getComprasStream()
            comprasStream.collect{ comprasActualizadas ->
                Log.v("ComprasViewModel", "ObtenerCompras() update{}")
                _uiState.update { currentState ->
                    currentState.copy(
                        compras = comprasActualizadas
                    )
                }
            }
        }
    }

    fun agregarCompra(compra:String){
        job = viewModelScope.launch {
            val c = Compra(UUID.randomUUID().toString(), compra)
            comprasRepository.insertar(c)
            _uiState.update {
                it.copy(mensaje = "Compra agregada: ${c.descripcion}")
            }
            obtenerCompras()
        }
    }

    fun eliminarCompra(compra:Compra) {
        job = viewModelScope.launch {
            comprasRepository.eliminar(compra)
            _uiState.update {
                it.copy(mensaje = "Tarea eliminada: ${compra.descripcion}")
            }
            obtenerCompras()
        }
    }
}