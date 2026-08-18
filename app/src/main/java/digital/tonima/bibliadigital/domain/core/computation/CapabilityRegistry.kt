package digital.tonima.bibliadigital.domain.core.computation

class CapabilityRegistry private constructor(
    private val capabilities: Map<Class<*>, Api>,
) {
    @Suppress("UNCHECKED_CAST")
    fun <C : Api> get(clazz: Class<C>): C =
        capabilities[clazz] as? C ?: throw IllegalArgumentException("Capability ${clazz.simpleName} not registered")

    class Builder {
        private val registry = mutableMapOf<Class<*>, Api>()

        fun <C : Api> register(
            clazz: Class<C>,
            instance: C,
        ) = apply {
            registry[clazz] = instance
        }

        fun build() = CapabilityRegistry(registry.toMap())
    }
}
