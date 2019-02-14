/**
 * Antlr grammar for a cryptocoin trading language (cctl)
 *
 * (c) 2013 Andreas Rueckert <a_rueckert@gmx.net>
 */

grammar cctl;
options {k=2; backtrack=true; memoize=true;}

@header {
package de.andreas_rueckert.trade.language.parser;
}

tokens {
    END = 'end';
    RULE = 'rule';
    RULESET = 'ruleset';
    THEN = 'then';
    WHEN = 'when';
}

rule 
    : RULE STRING WHEN CONDITION THEN CONSEQUENCE END
    ;



// Lexer

NumberLiteral // with day, hour, minute, second suffix...
    :   ('0'..'9')+ '.' ('0'..'9')* ( 'd' | 'h' | 'm' | 's' )?
    ;

StringLiteral
    :  '"' ( ~( '"' ) )* '"'
    ;

WS  : (' '|'\t')+ { skip(); } ;

