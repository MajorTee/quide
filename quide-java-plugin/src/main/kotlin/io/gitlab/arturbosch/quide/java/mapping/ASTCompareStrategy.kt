package io.gitlab.arturbosch.quide.java.mapping

import io.gitlab.arturbosch.quide.java.core.JavaCodeSmell
import io.gitlab.arturbosch.quide.mapping.SmellCompareStrategy

/**
 * @author Artur Bosch
 */
class ASTCompareStrategy : SmellCompareStrategy<JavaCodeSmell> {

	override fun matches(first: JavaCodeSmell, second: JavaCodeSmell): Boolean {
		return first.compare(second)
	}

	fun matchesRelocated(first: JavaCodeSmell, second: JavaCodeSmell): Boolean {
		return first.compareWithoutPath(second)
	}
}