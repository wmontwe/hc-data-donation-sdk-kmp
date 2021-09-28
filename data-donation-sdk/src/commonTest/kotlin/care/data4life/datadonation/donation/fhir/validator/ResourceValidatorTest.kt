/*
 * Copyright (c) 2021 D4L data4life gGmbH / All rights reserved.
 *
 * D4L owns all legal rights, title and interest in and to the Software Development Kit ("SDK"),
 * including any intellectual property rights that subsist in the SDK.
 *
 * The SDK and its documentation may be accessed and used for viewing/review purposes only.
 * Any usage of the SDK for other purposes, including usage for the development of
 * applications/third-party applications shall require the conclusion of a license agreement
 * between you and D4L.
 *
 * If you are interested in licensing the SDK for your own applications/third-party
 * applications and/or if you’d like to contribute to the development of the SDK, please
 * contact D4L by email to help@data4life.care.
 */

package care.data4life.datadonation.donation.fhir.validator

import care.data4life.datadonation.donation.fhir.AllowedReference
import care.data4life.datadonation.donation.fhir.wrapper.CompatibilityWrapperContract.Observation
import care.data4life.datadonation.donation.fhir.wrapper.CompatibilityWrapperContract.QuestionnaireResponse
import care.data4life.datadonation.donation.fhir.wrapper.CompatibilityWrapperContract.ResearchSubject
import care.data4life.datadonation.donation.fhir.wrapper.Fhir3Observation
import care.data4life.datadonation.donation.fhir.wrapper.Fhir3ObservationWrapper
import care.data4life.datadonation.donation.fhir.wrapper.Fhir3QuestionnaireResponse
import care.data4life.datadonation.donation.fhir.wrapper.Fhir3QuestionnaireResponseItemWrapper
import care.data4life.datadonation.donation.fhir.wrapper.Fhir3QuestionnaireResponseWrapper
import care.data4life.datadonation.donation.fhir.wrapper.Fhir3ResearchSubject
import care.data4life.datadonation.donation.fhir.wrapper.Fhir3ResearchSubjectWrapper
import care.data4life.datadonation.donation.program.model.QuestionnaireResponseBlur
import care.data4life.datadonation.mock.stub.donation.fhir.validator.ObservationValidatorStub
import care.data4life.datadonation.mock.stub.donation.fhir.validator.QuestionnaireResponseValidatorStub
import care.data4life.datadonation.mock.stub.donation.fhir.validator.ResearchSubjectValidatorStub
import care.data4life.hl7.fhir.FhirVersion
import care.data4life.hl7.fhir.stu3.codesystem.ObservationStatus
import care.data4life.hl7.fhir.stu3.codesystem.QuestionnaireResponseStatus
import care.data4life.hl7.fhir.stu3.codesystem.ResearchSubjectStatus
import care.data4life.hl7.fhir.stu3.model.CodeableConcept
import care.data4life.hl7.fhir.stu3.model.QuestionnaireResponseItem
import care.data4life.hl7.fhir.stu3.model.Reference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ResourceValidatorTest {
    @Test
    fun `It fulfils ResourceValidator`() {
        val filter: Any = ResourceValidator(
            QuestionnaireResponseValidatorStub(),
            ObservationValidatorStub(),
            ResearchSubjectValidatorStub()
        )

        assertTrue(filter is FhirResourceValidatorContract.ResourceValidator)
    }

    @Test
    fun `Given isAllowed is called with a arbitrary FhirResource, a StudyId and a BlurMapping, it returns false`() {
        // Given
        val resource = Fhir3QuestionnaireResponseItemWrapper(
            QuestionnaireResponseItem(linkId = "23")
        )
        val studyId = "Test"
        val mapping = mapOf(
            "something" to null
        )

        // When
        val result = ResourceValidator(
            QuestionnaireResponseValidatorStub(),
            ObservationValidatorStub(),
            ResearchSubjectValidatorStub()
        ).canBeDonated(
            resource,
            studyId,
            mapping
        )

        // Then
        assertFalse(result)
    }

    @Test
    fun `Given isAllowed is called with a QuestionnaireRespose, a StudyId and a BlurMapping, it delegates to call to QuestionnaireResposeValidator and returns its result`() {
        // Given
        val resource = Fhir3QuestionnaireResponseWrapper(
            Fhir3QuestionnaireResponse(
                status = QuestionnaireResponseStatus.COMPLETED
            )
        )
        val studyId = "Test"
        val mapping = mapOf(
            "something" to null
        )

        val expected = true

        val questionnaireResponseValidator = QuestionnaireResponseValidatorStub()

        var capturedResource: QuestionnaireResponse<out FhirVersion, out FhirVersion, out FhirVersion, out FhirVersion>? = null
        var capturedBlurMapping: Map<AllowedReference, QuestionnaireResponseBlur?>? = null

        questionnaireResponseValidator.whenIsAllowed = { delegatedResource, delegatedBlurMapping ->
            capturedResource = delegatedResource
            capturedBlurMapping = delegatedBlurMapping

            expected
        }

        // When
        val result = ResourceValidator(
            questionnaireResponseValidator,
            ObservationValidatorStub(),
            ResearchSubjectValidatorStub()
        ).canBeDonated(
            resource,
            studyId,
            mapping
        )

        // Then
        assertEquals(
            actual = result,
            expected = expected
        )

        assertSame(
            actual = capturedResource?.unwrap(),
            expected = resource.unwrap()
        )
        assertSame(
            actual = capturedBlurMapping,
            expected = mapping
        )
    }

    @Test
    fun `Given isAllowed is called with a Observation, a StudyId and a BlurMapping, it delegates to call to ObservationValidator and returns its result`() {
        // Given
        val resource = Fhir3ObservationWrapper(
            Fhir3Observation(
                status = ObservationStatus.AMENDED,
                code = CodeableConcept()
            )
        )
        val studyId = "Test"
        val mapping = mapOf(
            "something" to null
        )

        val expected = true

        val observationValidator = ObservationValidatorStub()

        var capturedResource: Observation<out FhirVersion, out FhirVersion>? = null
        var capturedBlurMapping: Map<AllowedReference, QuestionnaireResponseBlur?>? = null

        observationValidator.whenIsAllowed = { delegatedResource, delegatedBlurMapping ->
            capturedResource = delegatedResource
            capturedBlurMapping = delegatedBlurMapping

            expected
        }

        // When
        val result = ResourceValidator(
            QuestionnaireResponseValidatorStub(),
            observationValidator,
            ResearchSubjectValidatorStub()
        ).canBeDonated(
            resource,
            studyId,
            mapping
        )

        // Then
        assertEquals(
            actual = result,
            expected = expected
        )

        assertSame(
            actual = capturedResource?.unwrap(),
            expected = resource.unwrap()
        )
        assertSame(
            actual = capturedBlurMapping,
            expected = mapping
        )
    }

    @Test
    fun `Given isAllowed is called with a ResearchSubject, a StudyId and a BlurMapping, it delegates to call to ResearchSubjectValidator and returns its result`() {
        // Given
        val resource = Fhir3ResearchSubjectWrapper(
            Fhir3ResearchSubject(
                status = ResearchSubjectStatus.SUSPENDED,
                study = Reference(),
                individual = Reference()
            )
        )
        val studyId = "Test"
        val mapping = mapOf(
            "something" to null
        )

        val expected = true

        val researchSubjectValidator = ResearchSubjectValidatorStub()

        var capturedResource: ResearchSubject<out FhirVersion, out FhirVersion, out FhirVersion>? = null
        var capturedStudyId: String? = null

        researchSubjectValidator.whenIsAllowed = { delegatedResource, delegatedBlurMapping ->
            capturedResource = delegatedResource
            capturedStudyId = delegatedBlurMapping

            expected
        }

        // When
        val result = ResourceValidator(
            QuestionnaireResponseValidatorStub(),
            ObservationValidatorStub(),
            researchSubjectValidator
        ).canBeDonated(
            resource,
            studyId,
            mapping
        )

        // Then
        assertEquals(
            actual = result,
            expected = expected
        )

        assertSame(
            actual = capturedResource?.unwrap(),
            expected = resource.unwrap()
        )
        assertSame(
            actual = capturedStudyId,
            expected = studyId
        )
    }
}
