CPX #$0100
	BNE x_not_100
	LDX #$0200
	x_not_100:
	NOP
	NOP
	NOP
	NOP
	
	LDA $34; read char
	SEC
	SBC #$40;
	ASL
	ASL
	ASL
	ASL
	ASL
	TAY

	LDA #$08
	SEC
	SBC $73
	STA $74; 
	STA $75; 8-shift
	
	LDA #$10
	STA $76; loop counter
	
	LDA #$00 ; load 00 (top pixel line always empty)
	STA $7EF000,X
	LDA [$6D],Y ; load bot tile bytes
	bit_shift_left_first_byte_bot:
	ASL
	DEC $75
	BNE bit_shift_left_first_byte_bot
	STA $7EF100,X
	INX
	INY
	DEC $76
	
	loop_shift_width_over_8:
	
	LDA $74; 8-shift
	STA $75
	
	LDA [$6A],Y ; load top tile bytes
	bit_shift_left_new_top:
	ASL
	DEC $75
	BNE bit_shift_left_new_top
	STA $7EF000,X
	
	LDA $74; 8-shift
	STA $75
	LDA [$6D],Y ; load bot tile bytes
	bit_shift_left_new_bot:
	ASL
	DEC $75
	BNE bit_shift_left_new_bot
	STA $7EF100,X
	
	INX
	INY
	DEC $61
	BNE loop_shift_width_over_8
	RTL