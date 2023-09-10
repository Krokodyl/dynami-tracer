; main vwf routine
PHP
REP #$30
PHA
PHX
PHY
LDA #$0000
LDX $70
SEP #$20
LDA #$10
STA $76; loop counter
LDA $34; read char
SEC
SBC #$40;
ASL
ASL
ASL
ASL
ASL
TAY
;LDA $64
;CMP #$0F
;BPL copy_bytes_second_half

	LDA [$6A],Y;	load width
	STA $72;		save width
	LDA $73;		load shift

	BNE $06; shift > 0

	JSL $C30B00; jump to override routine
	
	BRA update_shift
	
	JSL $C30C00; jump to overlap routine
	
	
	LDA $73; load shift
	ADC $72; add width
	CMP #$08
	BMI $04; no overflow
	
	JSL $C30C80; jump to overflow routine
	

update_shift:
NOP

JSL $C30D00; jump to update shift

;INC $64
STX $70
REP #$30
PLY
PLX
PLA
PLP
RTL