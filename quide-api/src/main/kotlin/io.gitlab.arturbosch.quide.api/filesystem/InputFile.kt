package io.gitlab.arturbosch.quide.api.filesystem

import java.io.InputStream

/**
 * @author Artur Bosch
 */
interface InputFile : InputPath {
	val ending: String
	val content: String
	fun stream(): InputStream
}