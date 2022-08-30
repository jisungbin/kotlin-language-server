package org.javacs.kt.codeaction.quickfix

import org.eclipse.lsp4j.CodeAction
import org.eclipse.lsp4j.Command
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.javacs.kt.CompiledFile
import org.javacs.kt.index.SymbolIndex
import org.jetbrains.kotlin.resolve.diagnostics.Diagnostics
import org.jetbrains.kotlin.diagnostics.Diagnostic as KotlinDiagnostic

interface QuickFix {
    // Computes the quickfix. Return empty list if the quickfix is not valid or no alternatives exist.
    fun compute(file: CompiledFile, index: SymbolIndex, range: Range, diagnostics: List<Diagnostic>): List<Either<Command, CodeAction>>
}

fun diagnosticMatch(diagnostic: Diagnostic, range: Range, diagnosticTypes: Set<String>): Boolean =
    isDiagnosticInRange(diagnostic, range) && diagnosticTypes.contains(diagnostic.code.left)

// for a diagnostic to be in range the lines should be the same, and
//  the input character range should be within the bounds of the diagnostics range.
private fun isDiagnosticInRange(diagnostic: Diagnostic, range: Range): Boolean {
    val diagnosticRange = diagnostic.range
    return diagnosticRange.start.line == range.start.line && diagnosticRange.end.line == range.end.line &&
        diagnosticRange.start.character <= range.start.character && diagnosticRange.end.character >= range.end.character
}

fun diagnosticMatch(diagnostic: KotlinDiagnostic, startCursor: Int, endCursor: Int, diagnosticTypes: Set<String>): Boolean =
    diagnostic.textRanges.any { it.startOffset <= startCursor && it.endOffset >= endCursor } && diagnosticTypes.contains(diagnostic.factory.name)

fun findDiagnosticMatch(diagnostics: List<Diagnostic>, range: Range, diagnosticTypes: Set<String>) =
    diagnostics.find { diagnosticMatch(it, range, diagnosticTypes) }

fun anyDiagnosticMatch(diagnostics: Diagnostics, startCursor: Int, endCursor: Int, diagnosticTypes: Set<String>) =
    diagnostics.any { diagnosticMatch(it, startCursor, endCursor, diagnosticTypes) }
