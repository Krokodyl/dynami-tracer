LDA #$00 ; load 00 (top pixel line always empty)
	
	; STA $7EF000,X
	JSL $C30B80; jump to write top tile
	
	LDA [$6D],Y ; load bot tile bytes
	; STA $7EF100,X
	JSL $C30B90; jump to write top tile
	
	INX
	INY
	DEC $76
copy_bytes_first_half:
	LDA [$6A],Y ; load top tile bytes
	;STA $7EF000,X
	JSL $C30B80; jump to write top tile
	LDA [$6D],Y ; load bot tile bytes
	; STA $7EF100,X
	JSL $C30B90; jump to write top tile
	INX
	INY
	DEC $76
	BNE copy_bytes_first_half
	RTL