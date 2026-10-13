0 : {
		move(0,1.0);
		changeFace(1);
		fadeinAnimation( 275.0 , 275.0 , 275.0 , 4.5 , 0, 0.2) ;
	}
220 : {
		move(0,1.6);
		makeShapeModel("/effect/frzflush.oa",0,0.1,0.4,0,0,1,1);
		startAnimation( 275.0 , 276.0 , 276.0 , 6.0 , 0) ;
	}
320 : {
		attack("iceshoulder");
	}
750 : {
		move(0,0.8);
		startAnimation( 276.0 , 277.0 , 277.0 , 3.5 , 0) ;
		nextTech();
	}
920 : {
		recover();
	}
