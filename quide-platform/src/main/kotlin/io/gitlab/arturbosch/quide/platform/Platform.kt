package io.gitlab.arturbosch.quide.platform

import io.gitlab.arturbosch.kutils.awaitAll
import io.gitlab.arturbosch.kutils.cores
import io.gitlab.arturbosch.kutils.runAsync
import io.gitlab.arturbosch.kutils.withExecutor
import io.gitlab.arturbosch.kutils.withNamedThreadPoolExecutor
import io.gitlab.arturbosch.quide.vcs.VersionProvider
import io.gitlab.arturbosch.quide.vcs.Versionable

/**
 * @author Artur Bosch
 */
interface Platform {
	val plugins: List<Plugin>
	fun analyze()
	fun forEachPlugin(doBlock: (Plugin) -> Unit) {
		plugins.forEach(doBlock)
	}
}

class QuidePlatform(vcsLoader: VCSLoader,
					platform: BasePlatform) : Platform {

	override val plugins: List<Plugin>
		get() = executablePlatform.plugins

	private val logger by logFactory()
	private val executablePlatform: Platform

	init {
		val provider = vcsLoader.load()

		if (provider != null) {
			executablePlatform = MultiPlatform(platform, provider)
		} else {
			executablePlatform = platform
		}
	}

	override fun analyze() {
		logger.info("Starting $QUIDE ...")
		executablePlatform.analyze()
	}
}

class MultiPlatform(private val platform: BasePlatform,
					private val versionProvider: VersionProvider) : Platform {

	init {
		forEachPlugin { it.userData().put(VersionAware.VERSION_PROVIDER, versionProvider) }
	}

	override val plugins: List<Plugin>
		get() = platform.plugins

	private val logger by logFactory()

	override fun analyze() {
		var lastVersion: Versionable? = null
		var currentVersion = versionProvider.nextVersion()
		while (currentVersion.isPresent) {
			val current = currentVersion.get()
			logger.info("${current.versionNumber()} - ${current.revision().date()} - ${current.revision().message()}")
			forEachPlugin { it.userData().put(UserData.LAST_VERSION, lastVersion) }
			forEachPlugin { it.userData().put(UserData.CURRENT_VERSION, current) }
			platform.analyze()
			lastVersion = current
			currentVersion = versionProvider.nextVersion()
		}
	}
}

class BasePlatform(private val analysis: Analysis,
				   private val pluginLoader: PluginLoader) : ControlFlow, Platform {

	override val plugins: List<Plugin>
		get() = plugins()
	private val logger by logFactory()

	private val _plugins = lazy {
		pluginLoader.load()
	}

	private val cpuCores: Int = Math.min(plugins().size, cores)

	override fun plugins(): List<Plugin> {
		return _plugins.value
	}

	override fun analyze() {
		if (cpuCores < 1) {
			logger.info("No plugins available...shutting down!")
			return
		}
		run(analysis)
	}

	override fun run(context: AnalysisContext) {
		beforeAnalysis(analysis)
		withExecutor(withNamedThreadPoolExecutor(QUIDE, cpuCores)) {
			val futures = plugins().map { plugin ->
				runAsync {
					execute(plugin)
				}.exceptionally {
					logger.error("An error occurred while executing ${plugin.name()}", it)
				}
			}
			awaitAll(futures)
		}
		afterAnalysis()
	}
}