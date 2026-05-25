package br.com.fiap.inovacaogab.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.fiap.inovacaogab.model.IdeiaInovacao
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeiaDialog(
    onDismiss: () -> Unit,
    onConfirm: (IdeiaInovacao) -> Unit,
    ideia: IdeiaInovacao = IdeiaInovacao(),
    operacao: String
) {
    val gabBlueDark = Color(0xFF0A2540)
    val gabBlueLight = Color(0xFF0066CC)

    var tituloIdeia by remember { mutableStateOf(ideia.tituloIdeia) }
    var descricao by remember { mutableStateOf(ideia.descricao) }

    // Estados para o Dropdown de Setor
    var setorExpanded by remember { mutableStateOf(false) }
    var setor by remember { mutableStateOf(if (ideia.setor.isNotEmpty()) ideia.setor else "Passageiros") }
    val setoresOpcoes = listOf("Passageiros", "Logística", "Comércio", "Corporativo")

    // Estados para o Calendário (Date Picker)
    var dataRegistro by remember { mutableStateOf(ideia.dataRegistro) }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Oculta o teclado quando o calendário abrir
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // Ajusta o fuso horário para evitar erro de dia anterior
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
                        formatter.timeZone = TimeZone.getTimeZone("UTC")
                        dataRegistro = formatter.format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White,
        title = {
            Text(text = operacao, fontWeight = FontWeight.Bold, color = gabBlueDark)
        },
        text = {
            Column {
                // Título
                OutlinedTextField(
                    value = tituloIdeia,
                    onValueChange = { tituloIdeia = it },
                    label = { Text("Título da sugestão ou ideia") }, // TEXTO ATUALIZADO AQUI
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Menu Suspenso de Setor
                ExposedDropdownMenuBox(
                    expanded = setorExpanded,
                    onExpandedChange = { setorExpanded = !setorExpanded }
                ) {
                    OutlinedTextField(
                        value = setor,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Setor Afetado") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = setorExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = setorExpanded,
                        onDismissRequest = { setorExpanded = false }
                    ) {
                        setoresOpcoes.forEach { opcao ->
                            DropdownMenuItem(
                                text = { Text(opcao) },
                                onClick = {
                                    setor = opcao
                                    setorExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Descrição
                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição detalhada") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Campo de Data (Abre o Calendário ao clicar)
                OutlinedTextField(
                    value = dataRegistro,
                    onValueChange = {}, // Não permite digitar direto
                    readOnly = true,
                    enabled = false, // Impede o teclado de subir
                    label = { Text("Data do registro") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }, // Clique em qualquer lugar do campo abre o calendário
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = Color.Gray,
                        disabledLabelColor = Color.DarkGray
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val novaIdeia = IdeiaInovacao(
                        id = ideia.id,
                        autorId = ideia.autorId, // <--- ADICIONE ESTA LINHA! (É ela que salva a "identidade" do Operador)
                        tituloIdeia = tituloIdeia,
                        setor = setor,
                        descricao = descricao,
                        dataRegistro = dataRegistro,
                        foiAprovada = ideia.foiAprovada
                    )
                    onConfirm(novaIdeia)
                },
                colors = ButtonDefaults.buttonColors(containerColor = gabBlueLight)
            ) { Text("Salvar", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) { Text("Cancelar", color = Color.Gray) }
        }
    )
}