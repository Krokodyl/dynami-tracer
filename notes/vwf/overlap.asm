CPX #$0200
	BNE x_not_200
	LDX #$0100
	x_not_200:
	NOP
	NOP
	NOP
	REP #$30
	TXA
	SEC
	SBC #$0010
	TAX
	LDA #$0000
	REP #$10
	SEP #$20
	
	LDA #$08
	SEC
	SBC $73
	STA $74
	LDA #$FF
	bit_shift_left_top:
	ASL
	DEC $74
	BNE bit_shift_left_top
	STA $74; (0xFF << 8-shift)
	
	LDA #$10
	STA $76; loop counter

	loop_shift_width_under_8:
	LDA $7EF000,X ; load existing top tile bytes
	AND $74
	STA $7EF000,X
	LDA $7EF100,X ; load existing bot tile bytes
	AND $74
	STA $7EF100,X
	
	LDA $73 ; load shift
	STA $75 ; save tmp shift
	LDA [$6A],Y ; load top tile bytes
	
	bit_shift_right_top:
	LSR
	DEC $75
	BNE bit_shift_right_top
	STA $75
	LDA $7EF000,X
	ORA $75
	STA $7EF000,X
	
	LDA $73 ; load shift
	STA $75 ; save tmp
	LDA [$6D],Y ; load top tile bytes
	
	bit_shift_right_bot:
	LSR
	DEC $75
	BNE bit_shift_right_bot
	STA $75
	LDA $7EF100,X
	ORA $75
	STA $7EF100,X
	
	INX
	INY
	DEC $76
	BNE loop_shift_width_under_8
	RTL