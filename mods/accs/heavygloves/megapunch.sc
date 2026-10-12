0 : {
		stop();
		makeShapeModel("/effect/hadoushot.oa",0,0.25,0.5,0,0,1);
		changeFace(1);
		fadeinAnimation( 235.0 , 235.0 , 235.0 , 4.5 , 0, 0.2) ;
	}
260 : {
		move(0,2.0);
		startAnimation( 235.0 , 236.0 , 236.0 , 6.0 , 0) ;
	}
320 : {
		attack("megapunch1");
	}
420 : {
		makeShapeModel("/effect/flash.oa",0.2,-0.1,-0.5,0,1,0);
		attack("megapunch2");
		startAnimation( 236.0 , 238.0 , 236.0 , 24 , 1) ;
	}
560 : {
		move(0,1);
		nextTech();
	}
900 : {
		recover();
	}
