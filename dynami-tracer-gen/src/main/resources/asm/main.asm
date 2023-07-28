; 40 41 42 43 44 45 46 47 48 49 4A 4B 4C 4D 4E 4F 0B
; 40 41 42 43 44 45 46 47 48 49 4A 4B 4C 4D 4E 4F 05 50 51 52 53 54 55 56 57 58 59 5A 5B 5C 5D 5E 5F 05 60 61 62 63 64 65 66 67 68 69 6A 6B 6C 6D 6E 6F 0B
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
REP #$20
ASL
ASL
ASL
ASL
ASL
TAY
SEP #$20
;LDA $64
;CMP #$0F
;BPL copy_bytes_second_half

	LDA [$6A],Y;	load width
	STA $72;		save width
	LDA $73;		load shift

	BNE $06; shift > 0

	JSL $C30B00; jump to override routine
	
	BRA update_shift
	
	JSL $C30BD0; jump to overlap routine
	
	
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