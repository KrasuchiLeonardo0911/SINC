package com.sinc.mobile.domain.model.venta

enum class LogisticaStatus(val key: String, val label: String) {
    COMPROMETIDO("comprometido", "Comprometido"),
    RECOGIDO("recogido", "Recogido por transporte"),
    EN_MATADERO("en-matadero", "En planta de faena"),
    ENTREGADO("entregado", "Entregado y finalizado"),
    RECHAZADO_CARGA("rechazado-carga", "Rechazado en carga"),
    MATADERO_RECHAZADO("matadero-rechazado", "Rechazado en planta"),
    RECHAZADO_FINAL("rechazado-final", "Rechazado administrativo"),
    CANCELADO_REGRESANDO("cancelado-regresando", "Cancelado"),
    UNKNOWN("unknown", "Desconocido");

    companion object {
        fun fromKey(key: String): LogisticaStatus {
            return entries.find { it.key == key } ?: UNKNOWN
        }
    }
}
