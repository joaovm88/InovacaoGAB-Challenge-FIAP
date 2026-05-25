package br.com.fiap.inovacaogab.domain.ideia

import org.junit.Assert.assertEquals
import org.junit.Test

class ValidadorIdeia {

    //Função de lógica para validar os campos de uma ideia

    fun validarIdeia(titulo: String, setor: String, descricao: String): String {
        if (titulo.isBlank()) {
            return "O título não pode ser vazio"
        }
        if (setor.isBlank()) {
            return "O setor deve ser informado"
        }
        if (descricao.isBlank()) {
            return "A descrição não pode ser vazia"
        }
        if (descricao.length < 10) {
            return "A descrição deve ter pelo menos 10 caracteres"
        }

        return "Sucesso"
    }

    // Teste unitário para a função validarIdeia

    @Test
    fun validarIdeia_DadosValidos_RetornaSucesso() {
        val resultado = validarIdeia("Novo Projeto", "TI", "Descrição detalhada do projeto")
        assertEquals("Sucesso", resultado)
    }

    @Test
    fun validarIdeia_TituloVazio_RetornaErro() {
        val resultado = validarIdeia("", "Saúde", "Descrição longa")
        assertEquals("O título não pode ser vazio", resultado)
    }

    @Test
    fun validarIdeia_DescricaoCurta_RetornaErro() {
        val resultado = validarIdeia("App", "Educação", "Curta")
        assertEquals("A descrição deve ter pelo menos 10 caracteres", resultado)
    }
}
