package digital.tonima.bibliadigital.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow

/**
 * Utilitário para processar efeitos de UI de disparo único seguindo o padrão MVI.
 *
 * @param effectFlow O fluxo de efeitos (mapeado do estado).
 * @param onConsume Callback disparado imediatamente após o processamento do efeito para limpá-lo no ViewModel.
 * @param onEffect Ação a ser executada quando um novo efeito é emitido.
 */
@Composable
fun <E> LaunchedUiEffectHandler(
    effectFlow: Flow<E?>,
    onConsume: () -> Unit,
    onEffect: (E) -> Unit,
) {
    LaunchedEffect(effectFlow) {
        effectFlow.collect { effect ->
            if (effect != null) {
                onEffect(effect)
                onConsume()
            }
        }
    }
}
