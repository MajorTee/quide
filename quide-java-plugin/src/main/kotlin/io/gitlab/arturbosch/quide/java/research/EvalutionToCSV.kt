@file:Suppress("NOTHING_TO_INLINE")

package io.gitlab.arturbosch.quide.java.research

import io.gitlab.arturbosch.quide.java.core.JavaCodeSmell
import io.gitlab.arturbosch.quide.java.core.JavaSmellContainer
import io.gitlab.arturbosch.smartsmells.config.Smell

/**
 * @author Artur Bosch
 */

private val smellTypes = Smell.values().filter { it != Smell.UNKNOWN && it != Smell.CLASS_INFO }

fun evaluateToCSV(container: JavaSmellContainer): String {
	val codeSmells = container.all().groupBy { it.type }

	return with(StringBuilder()) {
		appendTotalSurvivalRatio(container)
		comment("START Metrics per SmellType")
		appendSmellTypes()
		appendSurvivalPerType(codeSmells)
		appendOccurrencePerType(codeSmells)
		appendLifespanPerType(codeSmells)
		appendChangesPerType(codeSmells)
		appendRelocationsPerType(codeSmells)
		appendRevivalsPerType(codeSmells)
		comment("END Metrics per SmellType")
		toString()
	}
}

private inline fun StringBuilder.comment(content: String) = append("--- $content ---\n")
private inline fun StringBuilder.content(content: String) = append("$content\n")

private inline fun StringBuilder.appendSmellTypes() {
	content(smellTypes.joinToString(","))
}

private inline fun StringBuilder.appendTotalSurvivalRatio(container: JavaSmellContainer) {
	comment("START Survival Ratio")
	val codeSmells = container.all()
	val allSize = codeSmells.size.toDouble()
	val alive = container.alive().size.toDouble()
	val dead = container.dead().size.toDouble()
	val alivePercentage = alive / allSize
	val deadPercentage = dead / allSize
	val ratio = alivePercentage / deadPercentage
	content("All, Alive, Dead, %Alive, %Dead, Ratio")
	content("$allSize,$alive,$dead,$alivePercentage,$deadPercentage,$ratio")
	comment("END Survival Ratio")
}

private inline fun StringBuilder.appendSurvivalPerType(codeSmells: Map<Smell, List<JavaCodeSmell>>) {
	comment("Type Survival")
	val result = smellTypes.map { type -> codeSmells[type] }
			.map { it ?: emptyList() }
			.map { calculateDeadPerAliveRatio(it) }
			.joinToString(",")
	content(result)
}

private inline fun calculateDeadPerAliveRatio(smells: List<JavaCodeSmell>): Double {
	if (smells.isEmpty()) return 0.0
	val allSize = smells.size.toDouble()
	val (alive, dead) = smells.partition { it.isAlive }
	val alivePercentage = alive.size / allSize
	val deadPercentage = dead.size / allSize
	return alivePercentage / deadPercentage
}

private fun StringBuilder.appendOccurrencePerType(codeSmells: Map<Smell, List<JavaCodeSmell>>) {
	comment("Type Occurrence")
	val occurrences = smellTypes.map { codeSmells[it]?.size ?: 0 }.joinToString(",")
	content(occurrences)
}

private fun StringBuilder.appendLifespanPerType(codeSmells: Map<Smell, List<JavaCodeSmell>>) {
	comment("Type Lifespan")
	val occurrences = smellTypes.asSequence()
			.map { codeSmells[it] }
			.map { it?.meanBy { it.lifespanInDays() } ?: 0.0 }
			.joinToString(",")
	content(occurrences)
}

private fun StringBuilder.appendChangesPerType(codeSmells: Map<Smell, List<JavaCodeSmell>>) {
	comment("Type Changes")
	val changes = smellTypes.asSequence()
			.map { codeSmells[it] }
			.map { it?.meanBy { it.weight() } ?: 0.0 }
			.joinToString(",")
	content(changes)
}

private fun StringBuilder.appendRelocationsPerType(codeSmells: Map<Smell, List<JavaCodeSmell>>) {
	comment("Type Relocations")
	val relocations = smellTypes.asSequence()
			.map { codeSmells[it] }
			.map { it?.meanBy { it.relocations().size } ?: 0.0 }
			.joinToString(",")
	content(relocations)
}

private fun StringBuilder.appendRevivalsPerType(codeSmells: Map<Smell, List<JavaCodeSmell>>) {
	comment("Type Revivals")
	val relocations = smellTypes.asSequence()
			.map { codeSmells[it] }
			.map { it?.meanBy { it.revivedInVersions().size } ?: 0.0 }
			.joinToString(",")
	content(relocations)
}

private fun List<JavaCodeSmell>.meanBy(by: (JavaCodeSmell) -> Int): Double = if (isEmpty()) 0.0
else map { by(it) }.sum().toDouble() / size.toDouble()

private fun JavaCodeSmell.lifespanInDays() = endVersion().versionNumber() - startVersion().versionNumber()