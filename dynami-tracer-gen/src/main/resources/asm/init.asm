; init/reset vwf variables
REP #$30
LDA $70
CMP #$0000
BNE not_offset_0; Mesen Bug : Off by one
LDA #$0010
STA $70
not_offset_0:
NOP
CMP #$0100
BNE not_offset_100; Mesen Bug : Off by one
LDA #$0200
STA $70
not_offset_100:
NOP
NOP
SEP #$30
LDA [$1A]; read char
CMP #$06
BNE 8
LDA #$40 ; shift reset + tab
STA $70
STZ $71
STZ $73; set shift to 0

CMP #$00
BEQ 10; shift_reset
CMP #$05
BMI 4; no_shift_reset
CMP #$19
BMI 2; shift_reset
BRA 8; no_shift_reset
shift_reset:
LDA #$10
STA $70
STZ $71
STZ $73; set shift to 0

no_shift_reset:
REP #$20
SEP #$10
LDA $10
STA $60
CLC
ADC #$0100
STA $63
LDA #$7EF0
STA $68
LDA #$4000
STA $6A
LDA #$10C3
STA $6C
LDA #$C340
STA $6E
LDA #$0000
SEP #$20
LDA $12
STA $62
STA $65
LDA [$1A]; read char
REP #$30
RTL