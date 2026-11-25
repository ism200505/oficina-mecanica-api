package br.umc.oficina.dominio.comum;

/** Objeto de valor quilometragem. Invariante: nunca negativa. */
public record Quilometragem(long km) {

    public static final Quilometragem ZERO = new Quilometragem(0);

    public Quilometragem {
        if (km < 0) {
            throw new RegraDeNegocioException("Quilometragem não pode ser negativa");
        }
    }

    public boolean menorQue(Quilometragem outra) {
        return km < outra.km;
    }

    public long percorridoDesde(Quilometragem anterior) {
        return km - anterior.km;
    }
}
