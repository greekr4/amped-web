0 : {
		move(0,1);
		changeFace(1);
		fadeinAnimation( 410.0 , 410.0 , 410.0 , 4.5 , 0, 0.2) ;
	}
120 : {
		makeParticle("fcharge.ptc",0.2,-0.1,-0.35,0,1,0);
	}
320 : {
		move(0,1,2.1);
		startAnimation( 410.0 , 412.0 , 412.0 , 5.0 , 0) ;
		makeShapeModel("/effect/explode1.oa",0,0.3,0.5,0,1,0);
	}
350 : {
		attack("blazeup");
	}
590 : {
		move(0,1);
	}
1000 : {
		recover();
	}
