0 : {
		stop();
		changeFace(6);
		fadeinAnimation( 115.0 , 115.0 , 105.0 , 8 , 0, 0.2) ;
		ignoreAttack(500);
	}
200 : {
		fadeinAnimation( 117.0 , 117.0 , 117.0 , 8 , 0, 0.1) ;
		makeParticle("fcharge.ptc",0.0,0.0,0.4,0,1,0);
	}
300 : {
		startAnimation( 117.0 , 118.0 , 117.0 , 4 , 1) ;
	}
700 : {
		fadeinAnimation( 235.0 , 236.0 , 236.0 , 6.0 , 0, 0.1) ;
		makeShapeModel("/effect/biglaser_fire.oa",0,0.2,0.6,0,0,1);
		attack("biglaser");
	}
1500 : {
		startAnimation( 236.0 , 238.0 , 236.0 , 24 , 1) ;
	}
1900 : {
		recover();
	}
