package com.ontimehealth.backend.service

import com.mercadopago.MercadoPagoConfig
import com.mercadopago.client.payment.PaymentClient
import com.mercadopago.client.preference.PreferenceBackUrlsRequest
import com.mercadopago.client.preference.PreferenceClient
import com.mercadopago.client.preference.PreferenceItemRequest
import com.mercadopago.client.preference.PreferenceRequest
import com.ontimehealth.backend.repository.TurnoRepository
import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class PagoService (
    private val turnoRepository: TurnoRepository,
    @Value("\${mercadopago.access-token}") private val accessToken: String,
    @Value("\${app.frontend-url}") private val frontendUrl: String,
    @Value("\${app.base-url}") private val backendUrl: String,
    )  {

    @PostConstruct
    fun init() {
        MercadoPagoConfig.setAccessToken(accessToken)
    }

    @Transactional
    fun crearPreferencia(usuarioId: Long, turnoId: Long): String {
        val turno = turnoRepository.findById(turnoId).orElseThrow(){
            IllegalArgumentException("Turno no encontrado")
        }

        if(turno.paciente?.usuario?.id != usuarioId) {
            throw IllegalArgumentException("este turno no pertenece")
        }
        if(turno.estadoPaciente != "ATENDIDO"){
            throw IllegalArgumentException("Todavia no se completo la consulta")
        }
        if(turno.estadoPago == "APROBADO"){
            throw IllegalArgumentException("Esta consulta ya fue pagada")
        }
        val precio = turno.profesional?.precioConsulta?: throw IllegalArgumentException("el medico no configuro el precio de consulta")

        val item = PreferenceItemRequest.builder()
            .title("Consulta medica - Dr/a. ${turno.profesional?.usuario?.nombre} ${turno.profesional?.usuario?.apellido}")
            .quantity(1)
            .currencyId("ARS")
            .unitPrice(precio)
            .build()

        val backUrls = PreferenceBackUrlsRequest.builder()
            .success("$frontendUrl/turnos?pago=success")
            .pending("$frontendUrl/turnos?pago=pending")
            .failure("$frontendUrl/turnos?pago=failure")
            .build()

        val request = PreferenceRequest.builder()
            .items(listOf(item))
            .backUrls(backUrls)
            .externalReference(turno.id.toString())
            .notificationUrl("$backendUrl/api/pagos/webhook")
            .build()

        val preference = try {
            PreferenceClient().create(request)
        } catch (e: com.mercadopago.exceptions.MPApiException) {
            throw IllegalArgumentException("MercadoPago error: ${e.apiResponse?.content}")
        }

        turno.mercadopagoPreferenceId = preference.id
        turno.estadoPago = "PENDIENTE"
        turnoRepository.save(turno)

        return preference.initPoint
    }

    @Transactional
    fun procesarNotificacion(paymentId: Long) {
        val payment = PaymentClient().get(paymentId)
        val turnoId = payment.externalReference?.toLongOrNull() ?: return
        val turno = turnoRepository.findById(turnoId).orElse(null) ?: return

        turno.mercadopagoPaymentId = paymentId.toString()
        turno.estadoPago = when (payment.status) {
            "approved" -> "APROBADO"
            "pending" -> "PENDIENTE"
            "in_process" -> "EN_PROCESO"
            "rejected" -> "RECHAZADO"
            else -> turno.estadoPago
        }
        turnoRepository.save(turno)
    }

    @Transactional
    fun confirmarPagoPorRedireccion(usuarioId: Long, turnoId: Long, status: String?) {
        val turno = turnoRepository.findById(turnoId).orElseThrow {
            IllegalArgumentException("Turno no encontrado")
        }
        if (turno.paciente?.usuario?.id != usuarioId) {
            throw IllegalArgumentException("Este turno no pertenece")
        }
        turno.estadoPago = when (status) {
            "approved" -> "APROBADO"
            "pending", "in_process" -> "PENDIENTE"
            "rejected" -> "RECHAZADO"
            else -> turno.estadoPago
        }
        turnoRepository.save(turno)
    }
}
