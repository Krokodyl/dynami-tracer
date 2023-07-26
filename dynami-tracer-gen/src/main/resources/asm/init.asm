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
CMP #$05
BNE no_shift_reset ; Mesen Bug : Off by one
STZ $60; set shift to 0
STZ $64
STZ $62
STZ $70
STZ $71
no_shift_reset:
NOP
NOP
NOP
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
REP #$20
RTL